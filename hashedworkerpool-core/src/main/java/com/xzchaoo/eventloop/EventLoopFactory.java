package com.xzchaoo.eventloop;

/**
 * @author xiangfeng.xzc
 * @date 2020-03-20
 */
@FunctionalInterface
public interface EventLoopFactory {
    EventLoop create(Object obj);
}
