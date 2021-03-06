package com.xzchaoo.eventloop.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.xzchaoo.eventloop.AbstractEventLoop;
import com.xzchaoo.eventloop.BatchPublish;
import com.xzchaoo.eventloop.Consumer;
import com.xzchaoo.eventloop.Event;
import com.xzchaoo.eventloop.EventLoopConfig;
import com.xzchaoo.eventloop.SingleThreadFactory;

import java.util.List;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
public class DisruptorEventLoop extends AbstractEventLoop {
    private final Disruptor<Event<Object>> disruptor;
    private final RingBuffer<Event<Object>> ringBuffer;

    DisruptorEventLoop(EventLoopConfig config, WaitStrategy waitStrategy, DisruptorEventLoopManager manager) {
        super(config, manager);
        int eventLoopBufferSize = config.getManagerConfig().getEventLoopBufferSize();
        SingleThreadFactory eventLoopThreadFactory = config.getEventLoopThreadFactory();
        this.disruptor = new Disruptor<>(() -> new Event<>(index), eventLoopBufferSize, eventLoopThreadFactory,
            ProducerType.MULTI, waitStrategy);
        // TODO
        // disruptor.setDefaultExceptionHandler();
        this.disruptor.handleEventsWith(this::onEvent);
        this.ringBuffer = disruptor.getRingBuffer();
    }

    private void onEvent(Event<Object> event, long sequence, boolean endOfBatch) {
        Consumer<Object> consumer = event.consumer;
        event.consumer = null;
        consumer.accept(event);
        // helps GC
        event.payload = null;
        event.arg1 = null;
        event.arg2 = null;
    }

    @Override
    protected void doStart() {
        disruptor.start();
    }

    @Override
    protected void doStop() {
        disruptor.shutdown();
    }

    @Override
    protected <P> void publish0(Object type, P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        long sequence = ringBuffer.next();
        Event<P> event = (Event<P>) ringBuffer.get(sequence);
        event.type = type;
        event.payload = payload;
        event.arg1 = arg1;
        event.arg2 = arg2;
        event.consumer = consumer;
        ringBuffer.publish(sequence);
    }

    @Override
    public void batchPublish(BatchPublish batch) {
        if (batch == null) {
            return;
        }
        List<BatchPublish.Event<?>> events = batch.getEvents();
        if (events.isEmpty()) {
            return;
        }
        int size = events.size();
        long hi = ringBuffer.next(size);
        long lo = hi - size + 1;
        long i = lo;
        for (BatchPublish.Event<?> event : events) {
            Event<Object> event2 = ringBuffer.get(i++);
            event2.type = event.type;
            event2.payload = event.payload;
            event2.arg1 = event.arg1;
            event2.arg2 = event.arg2;
            event2.consumer = (Consumer<Object>) event.consumer;
        }
        ringBuffer.publish(lo, hi);
    }
}
