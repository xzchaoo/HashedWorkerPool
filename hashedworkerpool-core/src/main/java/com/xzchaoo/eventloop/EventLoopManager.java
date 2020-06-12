package com.xzchaoo.eventloop;

import java.util.concurrent.ScheduledExecutorService;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
public interface EventLoopManager {

    default <P> void publish1(int hash, P payload, Consumer<P> consumer) {
        publish1(hash, payload, null, null, consumer);
    }

    default <P> void publish1(int hash, P payload, Object arg1, Consumer<P> consumer) {
        publish1(hash, payload, arg1, null, consumer);
    }

    <P> void publish1(int hash, P payload, Object arg1, Object arg2, Consumer<P> consumer);

    default <P> void broadcast1(P payload, Consumer<P> consumer) {
        broadcast1(payload, null, null, consumer);
    }

    default <P> void broadcast1(P payload, Object arg1, Consumer<P> consumer) {
        broadcast1(payload, arg1, null, consumer);
    }

    <P> void broadcast1(P payload, Object arg1, Object arg2, Consumer<P> consumer);

    default <P> void publish2(int hash, Object type, P payload) {
        publish2(hash, type, payload, null, null);
    }

    default <P> void publish2(int hash, Object type, P payload, Object arg1) {
        publish2(hash, type, payload, arg1, null);
    }

    <P> void publish2(int hash, Object type, P payload, Object arg1, Object arg2);

    default <P> void broadcast2(Object type, P payload) {
        broadcast2(type, payload, null, null);
    }

    default <P> void broadcast2(Object type, P payload, Object arg1) {
        broadcast2(type, payload, arg1, null);
    }

    <P> void broadcast2(Object type, P payload, Object arg1, Object arg2);

    default <P> void publish3(int hash, int type, P payload) {
        publish3(hash, type, payload, null, null);
    }

    default <P> void publish3(int hash, int type, P payload, Object arg1) {
        publish3(hash, type, payload, arg1, null);
    }

    <P> void publish3(int hash, int type, P payload, Object arg1, Object arg2);

    default <P> void broadcast3(int type, P payload) {
        broadcast3(type, payload, null, null);
    }

    default <P> void broadcast3(int type, P payload, Object arg1) {
        broadcast3(type, payload, arg1, null);
    }

    <P> void broadcast3(int type, P payload, Object arg1, Object arg2);

    EventLoop eventLoop(int size);

    EventLoop eventLoopByHash(int hash);

    void batchPublish(BatchPublish batch);

    void start();

    void stop();

    ScheduledExecutorService globalScheduler();

    int size();

    <P> void register2(Object type, ConsumerFactory<P> factory);

    <P> void register3(int type, ConsumerFactory<P> factory);

    void unregister3(int type);
}