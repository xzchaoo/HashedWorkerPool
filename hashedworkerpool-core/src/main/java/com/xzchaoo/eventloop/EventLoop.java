package com.xzchaoo.eventloop;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
public interface EventLoop {
    EventLoopManager manager();

    <P> void publish1(P payload, Consumer<P> consumer);

    <P> void publish1(P payload, Object arg1, Consumer<P> consumer);

    <P> void publish1(P payload, Object arg1, Object arg2, Consumer<P> consumer);

    <P> void publish2(Object type, P payload);

    <P> void publish2(Object type, P payload, Object arg1);

    <P> void publish2(Object type, P payload, Object arg1, Object arg2);

    void batchPublish(BatchPublish batch);

    void start();

    void stop();

    boolean inEventLoop();

    boolean inEventLoop(Thread thread);

    ScheduledExecutorService scheduler();

    ScheduledExecutorService globalScheduler();

    ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit);

    ScheduledFuture<?> schedule(java.util.function.Consumer<EventLoop> consumer, long delay, TimeUnit unit);

    ScheduledFuture<?> scheduledWithFixedDelay(Runnable runnable, long delay, long period, TimeUnit unit);

    ScheduledFuture<?> scheduledWithFixedDelay(java.util.function.Consumer<EventLoop> consumer, long delay, long period,
                                               TimeUnit unit);
}
