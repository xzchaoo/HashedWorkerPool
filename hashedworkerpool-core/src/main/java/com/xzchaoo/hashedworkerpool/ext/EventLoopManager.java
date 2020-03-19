package com.xzchaoo.hashedworkerpool.ext;

import java.util.concurrent.ScheduledExecutorService;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
public interface EventLoopManager {
    <P> void publish(int hash, Event<P> event);

    <P> void publish(int hash, P payload, Consumer<P> consumer);

    <P> void publish(int hash, P payload, Object arg1, Object arg2, Consumer<P> consumer);

    <P> void publish(int hash, int type, P payload);

    <P> void publish(int hash, int type, P payload, Object arg1, Object arg2);

    void start();

    void stop();

    ScheduledExecutorService globalScheduler();

    int size();
}
