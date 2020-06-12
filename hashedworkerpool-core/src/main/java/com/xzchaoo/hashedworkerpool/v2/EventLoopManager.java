package com.xzchaoo.hashedworkerpool.v2;

import com.xzchaoo.hashedworkerpool.core.Consumer;

import java.util.concurrent.ScheduledExecutorService;

/**
 * created at 2020/6/8
 *
 * @author xzchaoo
 */
public interface EventLoopManager {
    void start();

    default void stop() {
        stop(true);
    }

    void stop(boolean waitForAllToComplete);

    EventLoop eventLoopByIndex(int index);

    EventLoop eventLoopByHash(int hash);

    ScheduledExecutorService globalScheduler();

    int size();

    <P> void publish(int hash, P payload, Consumer<P> consumer);
}
