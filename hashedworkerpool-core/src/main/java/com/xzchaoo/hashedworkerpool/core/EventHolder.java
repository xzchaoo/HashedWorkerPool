package com.xzchaoo.hashedworkerpool.core;

/**
 * disruptor处理的事件模型
 *
 * @author xzchaoo
 */
public class EventHolder<P> {
    int         hash;
    int         index;
    P           payload;
    Consumer<P> consumer;

    public void reset() {
        hash = 0;
        index = 0;
        payload = null;
        consumer = null;
    }
}
