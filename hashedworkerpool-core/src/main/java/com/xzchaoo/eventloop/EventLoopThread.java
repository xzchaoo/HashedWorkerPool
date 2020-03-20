package com.xzchaoo.eventloop;

/**
 * @author xiangfeng.xzc
 * @date 2020-03-20
 */
public class EventLoopThread extends Thread {
    InternalThreadLocalMap internalThreadLocalMap = new InternalThreadLocalMap();

    public EventLoopThread(Runnable target, String name) {
        super(null, target, name);
    }
}
