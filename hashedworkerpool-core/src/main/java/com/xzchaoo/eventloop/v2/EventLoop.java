package com.xzchaoo.eventloop.v2;

import java.util.concurrent.ScheduledExecutorService;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
public interface EventLoop {
    <P> void publish(P payload, Consumer<P> consumer);

    <P> void publish(int type, P payload);
    
    EventLoopManager manager();

    int index();

    default boolean inEventLoop() {
        return inEventLoop(Thread.currentThread());
    }

    boolean inEventLoop(Thread thread);

    ScheduledExecutorService scheduler();
}
