package com.xzchaoo.eventloop;

import java.util.concurrent.atomic.AtomicInteger;

import com.xzchaoo.eventloop.EventLoopThread;

/**
 * TODO same as netty's FastThreadLocal
 *
 * @author xiangfeng.xzc
 */
public class FastThreadLocal<T> {
    private static final AtomicInteger INDEX = new AtomicInteger();
    private final        int           index = INDEX.getAndIncrement();

    public T get() {
        // TODO 我们的实现真得有比较快吗?
        // = instanceof + HashMap.get
        Thread thread = Thread.currentThread();
        if (thread instanceof EventLoopThread) {
            return (T) ((EventLoopThread) thread).internalThreadLocalMap.map.get(index);
        }
        throw new IllegalStateException();
    }

    public void set(T t) {
        Thread thread = Thread.currentThread();
        if (thread instanceof EventLoopThread) {
            ((EventLoopThread) thread).internalThreadLocalMap.map.put(index, t);
            return;
        }
        throw new IllegalStateException();
    }

    public void remove() {
        Thread thread = Thread.currentThread();
        if (thread instanceof EventLoopThread) {
            ((EventLoopThread) thread).internalThreadLocalMap.map.remove(index);
        }
        throw new IllegalStateException();
    }
}
