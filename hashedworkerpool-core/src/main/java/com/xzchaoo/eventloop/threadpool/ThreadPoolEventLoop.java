package com.xzchaoo.eventloop.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.xzchaoo.eventloop.AbstractEventLoop;
import com.xzchaoo.eventloop.Consumer;
import com.xzchaoo.eventloop.Event;
import com.xzchaoo.eventloop.EventLoopConfig;
import com.xzchaoo.eventloop.EventLoopManager;

/**
 * @author xiangfeng.xzc
 */
public class ThreadPoolEventLoop extends AbstractEventLoop {

    private final ThreadPoolExecutor executor;

    public ThreadPoolEventLoop(EventLoopConfig config, EventLoopManager manager) {
        super(config, manager);
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(config.getManagerConfig().getEventLoopBufferSize());
        executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, queue, config.getEventLoopThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    protected final <P> void publish0(Object type, P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        Event<P> event = new Event<>();
        event.index = index;
        event.type = type;
        event.payload = payload;
        event.arg1 = arg1;
        event.arg2 = arg2;
        executor.execute(() -> consumer.accept(event));
    }

    @Override
    protected final void doStart() {
        // nothing
    }

    @Override
    protected final void doStop() {
        executor.shutdownNow();
    }
}
