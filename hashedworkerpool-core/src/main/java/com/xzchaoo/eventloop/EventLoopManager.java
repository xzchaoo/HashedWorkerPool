package com.xzchaoo.eventloop;

import java.util.concurrent.ScheduledExecutorService;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
public interface EventLoopManager {

    <P> void publish1(int hash, P payload, Consumer<P> consumer);

    <P> void publish1(int hash, P payload, Object arg1, Consumer<P> consumer);

    <P> void publish1(int hash, P payload, Object arg1, Object arg2, Consumer<P> consumer);

    <P> void publish2(int hash, Object type, P payload);

    <P> void publish2(int hash, Object type, P payload, Object arg1);

    <P> void publish2(int hash, Object type, P payload, Object arg1, Object arg2);

    <P> void broadcast1(P payload, Consumer<P> consumer);

    <P> void broadcast1(P payload, Object arg1, Consumer<P> consumer);

    <P> void broadcast1(P payload, Object arg1, Object arg2, Consumer<P> consumer);

    <P> void broadcast2(Object type, P payload);

    <P> void broadcast2(Object type, P payload, Object arg1);

    <P> void broadcast2(Object type, P payload, Object arg1, Object arg2);

    EventLoop eventLoop(int size);

    EventLoop eventLoopByHash(int hash);

    void batchPublish(BatchPublish batch);

    void start();

    void stop();

    ScheduledExecutorService globalScheduler();

    int size();

    <P> void register(Object type, ConsumerFactory<P> factory);
}