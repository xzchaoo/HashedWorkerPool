package com.xzchaoo.eventloop;

/**
 * @author xiangfeng.xzc
 */
@FunctionalInterface
public interface EventLoopFactory {
    EventLoop create(Object obj);
}
