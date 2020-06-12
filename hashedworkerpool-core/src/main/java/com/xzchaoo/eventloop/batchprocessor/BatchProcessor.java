package com.xzchaoo.eventloop.batchprocessor;

import com.xzchaoo.eventloop.Consumer;
import com.xzchaoo.eventloop.Event;
import com.xzchaoo.eventloop.EventLoop;
import com.xzchaoo.eventloop.EventLoopManager;
import com.xzchaoo.eventloop.TypeIndex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

/**
 * created at 2020/3/21
 *
 * @author xzchaoo
 */
public class BatchProcessor<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessor.class);
    private static final int STATE_NEW = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_STOPPED = 2;
    private static final Object FLUSH = new Object();
    private static final Object ARG2_IS_LIST = new Object();
    private static final Object ARG2_IS_LIST_STANDALONE = new Object();
    private final int identity = TypeIndex.next();
    private final EventLoopManager manager;
    private final IntFunction<Flusher<T>> flusherFactory;
    private final BatchProcessorConfig config;
    private final int concurrency;
    private final boolean blocking;
    private int hash;
    private ScheduledFuture<?> scheduledFuture;
    private final int bufferSize;
    private final AtomicInteger state = new AtomicInteger(STATE_NEW);
    private final Semaphore concurrencySemaphore;

    BatchProcessor(EventLoopManager manager,
                   BatchProcessorConfig config,
                   IntFunction<Flusher<T>> flusherFactory) {
        this.manager = Objects.requireNonNull(manager);
        this.config = Objects.requireNonNull(config);
        this.flusherFactory = Objects.requireNonNull(flusherFactory);
        int concurrency = config.getConcurrency();
        if (concurrency <= 0) {
            concurrency = Runtime.getRuntime().availableProcessors() * 4;
        }
        this.concurrency = concurrency;
        int bufferSize = config.getBufferSize();
        if (bufferSize <= 0) {
            bufferSize = 1024;
        }
        this.bufferSize = bufferSize;
        this.blocking = config.isBlocking();
        this.concurrencySemaphore = new Semaphore(concurrency);
    }

    public void start() {
        if (state.compareAndSet(STATE_NEW, STATE_STARTED)) {
            manager.register3(identity, Handler::new);
            Duration duration = config.getForceFlushInterval();
            scheduledFuture = manager.globalScheduler()
                .scheduleWithFixedDelay(this::flush, duration.toMillis(), duration.toMillis(), TimeUnit.MILLISECONDS);
        } else {
            if (state.get() == STATE_STARTED) {
                LOGGER.info("This BatchProcessor has been started");
            } else {
                LOGGER.info("This BatchProcessor has been stopped");
            }
        }
    }

    public void stop() {
        if (state.compareAndSet(STATE_STARTED, STATE_STOPPED)) {
            this.manager.unregister3(identity);
            ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
            this.scheduledFuture = null;
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
        } else {
            if (state.get() == STATE_NEW) {
                LOGGER.info("This BatchProcessor has not been started");
            } else {
                LOGGER.info("This BatchProcessor has been stopped");
            }
        }
    }

    public void flush() {
        // 我们约定arg1==null 是flush的信号
        manager.broadcast3(identity, null, FLUSH);
    }

    public void put(T t) {
        int hash = this.hash++;
        manager.publish3(hash, identity, t);
    }

    public void put(List<T> c) {
        int hash = this.hash++;
        int size = c.size();
        if (size <= bufferSize) {
            manager.publish3(hash, identity, null, ARG2_IS_LIST, c);
        } else {
            int fromIndex = 0;
            while (true) {
                int toIndex = Math.min(fromIndex + bufferSize, size);
                List<T> subBuffer = c.subList(fromIndex, toIndex);
                manager.publish3(hash++, identity, null, ARG2_IS_LIST_STANDALONE, subBuffer);
                if (toIndex == size) {
                    break;
                }
                fromIndex = toIndex;
            }
        }
    }

    private class Handler implements Consumer<T> {
        private final List<T> buffer = new ArrayList<>(bufferSize * 2);
        private final Flusher<T> flusher;
        private final EventLoop eventLoop;

        private Handler(EventLoop eventLoop) {
            this.eventLoop = eventLoop;
            this.flusher = flusherFactory.apply(eventLoop.index());
        }

        @Override
        public void accept(Event<T> event) {
            if (event.arg1 == null) {
                // normal case
                buffer.add(event.payload);
                if (buffer.size() >= bufferSize) {
                    flush();
                }
            } else if (event.arg1 == FLUSH) {
                if (buffer.size() > 0) {
                    flush();
                }
            } else if (event.arg1 == ARG2_IS_LIST) {
                List<T> arg2 = (List<T>) event.arg2;
                buffer.addAll(arg2);
                if (buffer.size() >= bufferSize) {
                    flush();
                }
            } else if (event.arg1 == ARG2_IS_LIST_STANDALONE) {
                // arg2 是 list 并且立即执行
                List<T> arg2 = (List<T>) event.arg2;
                flush0(arg2);
            }
        }

        private void flush() {
            // 处理是异步的 这里必须复制一份list
            List<T> bufferCopy = new ArrayList<>(buffer);
            buffer.clear();
            flush0(bufferCopy);
        }

        private void flush0(List<T> buffer) {
            if (blocking) {
                try {
                    concurrencySemaphore.acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("semaphore acquire interrupted", e);
                }
                flusher.flush(buffer, new Context(eventLoop));
            } else {
                if (concurrencySemaphore.tryAcquire()) {
                    flusher.flush(buffer, new Context(eventLoop));
                } else {
                    flusher.onMissingSemaphore(buffer, new Context(eventLoop, false));
                }
            }
        }
    }

    private class Context implements Flusher.Context<T> {
        private final EventLoop eventLoop;
        private final boolean releaseSemaphore;
        private int retryCount;

        public Context(EventLoop eventLoop) {
            this(eventLoop, true);
        }

        public Context(EventLoop eventLoop, boolean releaseSemaphore) {
            this.eventLoop = eventLoop;
            this.releaseSemaphore = releaseSemaphore;
        }

        @Override
        public void complete() {
            if (releaseSemaphore) {
                concurrencySemaphore.release();
            }
        }

        @Override
        public EventLoop eventLoop() {
            return eventLoop;
        }

        @Override
        public Semaphore semaphore() {
            return concurrencySemaphore;
        }

        @Override
        public void retry(long delayMills, List<T> buffer) {
            ++retryCount;
            if (releaseSemaphore) {
                concurrencySemaphore.release();
            }
            eventLoop.scheduler()
                .schedule(() -> eventLoop.publish3(identity, null, ARG2_IS_LIST_STANDALONE, buffer), delayMills,
                    TimeUnit.MILLISECONDS);
        }

        @Override
        public int getMaxRetryCount() {
            return config.getMaxRetryCount();
        }

        @Override
        public int getRetryCount() {
            return retryCount;
        }
    }
}
