package com.xzchaoo.eventloop;

/**
 * @author xiangfeng.xzc
 */
public class EventLoopThread extends Thread {
    InternalThreadLocalMap internalThreadLocalMap = new InternalThreadLocalMap();

    public EventLoopThread(Runnable target, String name) {
        super(null, target, name);
    }
}
