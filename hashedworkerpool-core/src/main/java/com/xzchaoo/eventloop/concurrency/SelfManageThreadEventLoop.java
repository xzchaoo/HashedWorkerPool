package com.xzchaoo.eventloop.concurrency;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.xzchaoo.eventloop.AbstractEventLoop;
import com.xzchaoo.eventloop.Consumer;
import com.xzchaoo.eventloop.EventLoopConfig;
import com.xzchaoo.eventloop.EventLoopManager;

/**
 * @author xiangfeng.xzc
 * @date 2020-03-20
 */
public abstract class SelfManageThreadEventLoop extends AbstractEventLoop {
    private       AtomicBoolean needNotify      = new AtomicBoolean();
    private final Lock          lock            = new ReentrantLock();
    private final Condition     notifyCondition = lock.newCondition();

    public SelfManageThreadEventLoop(EventLoopConfig config, EventLoopManager manager) {
        super(config, manager);
        // TODO 这里有必要搞一个 Executor 吗? 直接new一个线程不就行了?
        super.setEventLoopThread(config.getEventLoopThreadFactory().newThread(this::runInThread));
    }

    @Override
    protected <P> void publish0(Object type, P payload, Object arg1, Object arg2, Consumer<P> consumer) {

    }

    @Override
    protected void doStart() {
        Thread t = super.eventLoopThread;
        if (t != null) {
            t.start();
        }
    }

    protected abstract void runInThread();

    @Override
    protected void doStop() {
        Thread t = super.eventLoopThread;
        if (t != null) {
            t.interrupt();
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected void waitForMore() {
        needNotify.set(true);
        lock.lock();
        try {
            if (needNotify.get()) {
                try {
                    notifyCondition.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    protected void notifyMore() {
        if (needNotify.getAndSet(false)) {
            lock.lock();
            try {
                notifyCondition.signal();
            } finally {
                lock.unlock();
            }
        }
    }
}
