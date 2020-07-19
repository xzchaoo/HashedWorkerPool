package com.xzchaoo.eventloop.v2;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
@FunctionalInterface
public interface TypeConsumerFactory<P> {
    Consumer<P> create(EventLoop eventLoop);
}
