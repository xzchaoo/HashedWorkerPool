package com.xzchaoo.eventloop.v2;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
public class DisruptorEventLoop implements EventLoop {
    private final DisruptorEventLoopManager manager;
    private final int index;
    private final Disruptor<Event<?>> disruptor;
    private final RingBuffer<Event<?>> ringBuffer;
    private final boolean blockOnInsufficientCapacity;
    private final ScheduledThreadPoolExecutor scheduler;
    private volatile Map<Integer, Consumer<?>> consumerMap = new HashMap<>();
    private Thread thread;

    public DisruptorEventLoop(DisruptorEventLoopManager manager, //
                              int index, //
                              WaitStrategy waitStrategy) {
        this.manager = manager;
        this.index = index;
        this.blockOnInsufficientCapacity = manager.config.isBlockOnInsufficientCapacity();
        this.disruptor = new Disruptor<>(Event::new, //
            manager.config.getBufferSize(), //
            new OneThreadFactory(manager.config.getName() + "-EL-" + index), //
            ProducerType.MULTI, //
            waitStrategy);
        this.disruptor.setDefaultExceptionHandler(new LogAndIgnoreExceptionHandler());
        this.disruptor.handleEventsWith(new EventEventHandler());
        this.ringBuffer = this.disruptor.getRingBuffer();
        this.scheduler = new ScheduledThreadPoolExecutor(1,
            new OneThreadFactory(manager.config.getName() + "-ELS-" + index));
    }

    @Override
    public DisruptorEventLoopManager manager() {
        return manager;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public <P> void publish(P payload, Consumer<P> consumer) {
        long cursor;
        if (blockOnInsufficientCapacity) {
            cursor = ringBuffer.next();
        } else {
            try {
                cursor = ringBuffer.tryNext();
            } catch (InsufficientCapacityException e) {
                throw new IllegalStateException("EventLoop " + index + " has not available capacity");
            }
        }
        Event<P> event = (Event) ringBuffer.get(cursor);
        event.payload = payload;
        event.consumer = consumer;
        ringBuffer.publish(cursor);
    }

    @Override
    public <P> void publish(int type, P payload) {
        Consumer<Object> consumer = (Consumer) consumerMap.get(type);
        if (consumer != null) {
            publish(payload, consumer);
        }
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return this.thread == thread;
    }

    @Override
    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    void start() {
        disruptor.start();
        CountDownLatch cdl = new CountDownLatch(1);
        publish(null, (el, ignored) -> {
            this.thread = Thread.currentThread();
            cdl.countDown();
        });
        try {
            if (!cdl.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("fail to get thread in 2s");
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("fail to get thread in 2s", e);
        }
    }


    void stop() {
        scheduler.shutdownNow();
        disruptor.shutdown();
    }

    synchronized <P> void registry(int type, Consumer<P> consumer) {
        Map<Integer, Consumer<?>> consumerMap = new HashMap<>(this.consumerMap);
        if (consumerMap.putIfAbsent(type, consumer) != null) {
            throw new IllegalStateException();
        }
        this.consumerMap = consumerMap;
    }

    private class EventEventHandler implements EventHandler<Event<?>> {
        @Override
        public void onEvent(Event<?> event, long sequence, boolean endOfBatch) throws Exception {
            Consumer<Object> consumer = (Consumer) event.consumer;
            try {
                consumer.consume(DisruptorEventLoop.this, event.payload);
            } finally {
                event.payload = null;
                event.consumer = null;
            }
        }
    }
}
