package com.xzchaoo.eventloop.v2;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
@FunctionalInterface
public interface Consumer<P> {
    void consume(EventLoop eventLoop, P payload);
}
