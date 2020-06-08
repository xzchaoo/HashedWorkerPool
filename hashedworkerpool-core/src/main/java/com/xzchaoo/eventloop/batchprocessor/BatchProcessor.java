package com.xzchaoo.eventloop.batchprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xzchaoo.eventloop.Consumer;
import com.xzchaoo.eventloop.Event;
import com.xzchaoo.eventloop.EventLoop;
import com.xzchaoo.eventloop.EventLoopManager;

/**
 * created at 2020/3/21
 *
 * @author xzchaoo
 */
public class BatchProcessor<T> {
    private static final Logger                  LOGGER                  = LoggerFactory.getLogger(BatchProcessor.class);
    private static final Object                  FLUSH                   = new Object();
    private final        Object                  identity                = new Object();
    private final        EventLoopManager        manager;
    private final        IntFunction<Flusher<T>> flusherFactory;
    private              int                     hash;
    private              ScheduledFuture<?>      scheduledFuture;
    private final        int                     maxBatchSize            = 100;
    private final        int                     maxConcurrencyPerThread = 10;

    BatchProcessor(EventLoopManager manager, IntFunction<Flusher<T>> flusherFactory) {
        this.manager = Objects.requireNonNull(manager);
        this.flusherFactory = Objects.requireNonNull(flusherFactory);
    }

    public void start() {
        manager.register(identity, Handler::new);
        scheduledFuture = manager.globalScheduler()
                .scheduleWithFixedDelay(this::flush, 1, 1, TimeUnit.SECONDS);
    }

    public void flush() {
        manager.broadcast2(identity, null, FLUSH);
    }

    public void stop() {
        ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    public void put(T t) {
        int hash = this.hash++;
        manager.publish2(hash, identity, t);
    }

    public void put(List<T> c) {
        // 这里有冲突不要紧, 反正都是随机的
        int hash = this.hash++;
        int size = c.size();
        if (size <= maxBatchSize) {
            manager.publish2(hash, identity, null, c);
        } else {
            int fromIndex = 0;
            while (true) {
                int toIndex = Math.min(fromIndex + maxBatchSize, size);
                List<T> subBuffer = c.subList(fromIndex, toIndex);
                manager.publish2(hash++, identity, null, subBuffer);
                if (toIndex == size) {
                    break;
                }
                fromIndex = toIndex;
            }
        }
    }

    private class Handler implements Consumer<T>, Flusher.Context<T> {
        private final List<T>    buffer = new ArrayList<>(maxBatchSize * 2);
        private final Flusher<T> flusher;
        private final Semaphore  semaphore;
        private final EventLoop  eventLoop;

        private Handler(int index) {
            eventLoop = manager.eventLoop(index);
            flusher = flusherFactory.apply(index);
            semaphore = new Semaphore(maxConcurrencyPerThread);
        }

        @Override
        public void accept(Event<T> event) {
            if (event.arg1 == FLUSH) {
                if (buffer.size() > 0) {
                    flush();
                }
            } else if (event.arg1 != null) {
                List<T> arg1 = (List<T>) event.arg1;
                buffer.addAll(arg1);
                if (buffer.size() >= maxBatchSize) {
                    flush();
                }
            } else {
                buffer.add(event.payload);
                if (buffer.size() >= maxBatchSize) {
                    flush();
                }
            }
        }

        private void flush() {
            List<T> bufferCopy = new ArrayList<>(buffer);
            buffer.clear();
            // 能不能复用 bufferCopy?
            if (semaphore.tryAcquire()) {
                flusher.flush(bufferCopy, this);
            } else {
                flusher.onMissingSemaphore(bufferCopy, semaphore, this);
            }
        }

        @Override
        public void complete() {
            semaphore.release();
        }

        @Override
        public void retry(Throwable e, long delayMills, List<T> buffer) {
            semaphore.release();
            // TODO log
            eventLoop.schedule(() -> eventLoop.publish2(identity, null, buffer), delayMills, TimeUnit.MILLISECONDS);
        }

        @Override
        public void error(Throwable e) {
            semaphore.release(); // just log
        }
    }
}
