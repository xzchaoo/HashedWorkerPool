package com.xzchaoo.hashedworkerpool.ext.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.xzchaoo.hashedworkerpool.ext.AbstractEventLoop;
import com.xzchaoo.hashedworkerpool.ext.Consumer;
import com.xzchaoo.hashedworkerpool.ext.Event;
import com.xzchaoo.hashedworkerpool.ext.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
public class DisruptorEventLoop extends AbstractEventLoop {
    private final Disruptor<Event<Object>> disruptor;
    private final RingBuffer<Event<Object>> ringBuffer;

    DisruptorEventLoop(String name, int index, int ringBufferSize,
                       WaitStrategy waitStrategy,
                       DisruptorEventLoopManager manager) {
        super(name, index, manager);
        ThreadFactory disruptorThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat(name + "-EventLoop")
            .build();
        disruptor = new Disruptor<>(Event::new, ringBufferSize, disruptorThreadFactory, ProducerType.MULTI,
            waitStrategy);
        disruptor.handleEventsWith(this::onEvent);
        ringBuffer = disruptor.getRingBuffer();
    }

    private void onEvent(Event<Object> event, long sequence, boolean endOfBatch) {
        event.consumer.accept(event);
        // helps GC
        event.payload = null;
        event.arg1 = null;
        event.arg2 = null;
        event.consumer = null;
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
    protected <P> void publish(int type, P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        long sequence = ringBuffer.next();
        Event<P> event = (Event<P>) ringBuffer.get(sequence);
        event.index = index;
        event.type = type;
        event.payload = payload;
        event.arg1 = arg1;
        event.arg2 = arg2;
        event.consumer = consumer;
        ringBuffer.publish(sequence);
    }

    @Override
    public <P> void publish(Event<P> event) {
        long sequence = ringBuffer.next();
        Event<P> event2 = (Event<P>) ringBuffer.get(sequence);
        event2.index = index;
        event2.type = event.type;
        event2.payload = event.payload;
        event2.arg1 = event.arg1;
        event2.arg2 = event.arg2;
        event2.consumer = event.consumer;
        ringBuffer.publish(sequence);
    }
}
