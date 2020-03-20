package com.xzchaoo.eventloop;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xiangfeng.xzc
 * @date 2020-03-20
 */
public class SingleThreadFactory implements ThreadFactory {
    private final String           name;
    private final AtomicBoolean    done = new AtomicBoolean();
    @Getter
    @Setter
    private       Consumer<Thread> notify;

    public SingleThreadFactory(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public Thread newThread(Runnable r) {
        if (done.compareAndSet(false, true)) {
            Thread thread = new EventLoopThread(r, name);
            Consumer<Thread> notify = this.notify;
            if (notify != null) {
                notify.accept(thread);
            }
            return thread;
        }
        throw new IllegalStateException();
    }
}
