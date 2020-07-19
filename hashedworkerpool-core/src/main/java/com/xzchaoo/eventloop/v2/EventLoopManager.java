package com.xzchaoo.eventloop.v2;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
public interface EventLoopManager {
    <P> void publish(int hash, P payload, Consumer<P> consumer);

    <P> void broadcast(P payload, Consumer<P> consumer);

    <P> void publish(int hash, int type, P payload);

    <P> void broadcast(int type, P payload);

    <P> void register(int type, TypeConsumerFactory<P> factory);

    void start();

    void stop();

    EventLoop eventLoop(int index);

    EventLoop eventLoopByHash(int hash);

    int size();
}
