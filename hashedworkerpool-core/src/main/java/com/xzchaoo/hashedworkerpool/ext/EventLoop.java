package com.xzchaoo.hashedworkerpool.ext;

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

    <P> void publish(Event<P> event);

    <P> void publish(P payload, Consumer<P> consumer);

    <P> void publish(P payload, Object arg1, Object arg2, Consumer<P> consumer);

    <P> void publish(int type, P payload);

    <P> void publish(int type, P payload, Object arg1, Object arg2);

    void start();

    void stop();

    ScheduledExecutorService scheduler();

    ScheduledExecutorService globalScheduler();

    ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit);

    ScheduledFuture<?> schedule(java.util.function.Consumer<EventLoop> consumer, long delay, TimeUnit unit);

    ScheduledFuture<?> scheduledWithFixedDelay(Runnable runnable, long delay, long period, TimeUnit unit);

    ScheduledFuture<?> scheduledWithFixedDelay(java.util.function.Consumer<EventLoop> consumer, long delay, long period,
                                               TimeUnit unit);
}
