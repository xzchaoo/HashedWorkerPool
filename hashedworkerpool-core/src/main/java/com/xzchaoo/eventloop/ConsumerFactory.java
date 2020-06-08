package com.xzchaoo.eventloop;

/**
 * consumer factory
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
@FunctionalInterface
public interface ConsumerFactory<P> {
    /**
     * create consumer for event loop
     *
     * @param eventLoop event loop
     * @return consumer for that event loop
     */
    Consumer<P> create(EventLoop eventLoop);
}
