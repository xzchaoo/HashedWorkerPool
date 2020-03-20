package com.xzchaoo.eventloop.threadpool;

import com.xzchaoo.eventloop.AbstractEventLoop;
import com.xzchaoo.eventloop.AbstractEventLoopManager;
import com.xzchaoo.eventloop.EventLoopConfig;
import com.xzchaoo.eventloop.EventLoopManagerConfig;

/**
 * @author xiangfeng.xzc
 * @date 2020-03-20
 */
public class ThreadPoolEventLoopManager extends AbstractEventLoopManager {

    public ThreadPoolEventLoopManager(EventLoopManagerConfig p) {
        super(p);
        initEventLoop();
    }

    @Override
    protected AbstractEventLoop createEventLoop(EventLoopConfig config) {
        return new ThreadPoolEventLoop(config, this);
    }

    @Override
    public ThreadPoolEventLoop eventLoop(int index) {
        return (ThreadPoolEventLoop) super.eventLoop(index);
    }
}
