package com.xzchaoo.hashedworkerpool.v2;

import com.xzchaoo.hashedworkerpool.core.Consumer;

import java.util.concurrent.ScheduledExecutorService;

/**
 * created at 2020/6/8
 *
 * @author xzchaoo
 */
public interface EventLoop {
    EventLoopManager manager();

    <P> void publish(P payload, Consumer<P> consumer);

    ScheduledExecutorService scheduler();

    // something like disruptor's RingBuffer ?
    long next();

    long tryNext();

    Object get(long cursor);

    void publish(long cursor);
}
