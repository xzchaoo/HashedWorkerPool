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

    int index();

    ScheduledExecutorService scheduler();

    ScheduledExecutorService globalScheduler();

    default <P> void publish1(P payload, Consumer<P> consumer) {
        publish1(payload, null, null, consumer);
    }

    default <P> void publish1(P payload, Object arg1, Consumer<P> consumer) {
        publish1(payload, arg1, null, consumer);
    }

    <P> void publish1(P payload, Object arg1, Object arg2, Consumer<P> consumer);

    default <P> void publish2(Object type, P payload) {
        publish2(type, payload, null, null);
    }

    default <P> void publish2(Object type, P payload, Object arg1) {
        publish2(type, payload, arg1, null);
    }

    <P> void publish2(Object type, P payload, Object arg1, Object arg2);

    default <P> void publish3(int type, P payload) {
        publish3(type, payload, null, null);
    }

    default <P> void publish3(int type, P payload, Object arg1) {
        publish3(type, payload, arg1, null);
    }

    <P> void publish3(int type, P payload, Object arg1, Object arg2);

    void batchPublish(BatchPublish batch);

    void start();

    void stop();

    boolean inEventLoop();

    boolean inEventLoop(Thread thread);

    ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit);

    ScheduledFuture<?> schedule(java.util.function.Consumer<EventLoop> consumer, long delay, TimeUnit unit);

    ScheduledFuture<?> scheduledWithFixedDelay(Runnable runnable, long delay, long period, TimeUnit unit);

    ScheduledFuture<?> scheduledWithFixedDelay(java.util.function.Consumer<EventLoop> consumer, long delay, long period,
                                               TimeUnit unit);

    <P> void register2(Object type, ConsumerFactory<P> factory);

    <P> void register3(int type, ConsumerFactory<P> factory);

    void unregister3(int type);
}
