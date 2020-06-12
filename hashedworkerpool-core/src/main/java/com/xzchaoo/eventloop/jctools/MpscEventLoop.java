package com.xzchaoo.eventloop.jctools;

import org.jctools.queues.MpscArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xzchaoo.eventloop.Consumer;
import com.xzchaoo.eventloop.Event;
import com.xzchaoo.eventloop.EventLoopConfig;
import com.xzchaoo.eventloop.EventLoopManager;
import com.xzchaoo.eventloop.concurrency.SelfManageThreadEventLoop;

/**
 * @author xiangfeng.xzc
 */
public class MpscEventLoop extends SelfManageThreadEventLoop {
    private static final Logger                        LOGGER = LoggerFactory.getLogger(MpscEventLoop.class);
    private final        MpscArrayQueue<Event<Object>> q;

    public MpscEventLoop(EventLoopConfig config, EventLoopManager manager) {
        super(config, manager);
        q = new MpscArrayQueue<>(config.getManagerConfig().getEventLoopBufferSize());
    }

    @Override
    protected <P> void publish0(Object type, P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        Event<P> event = new Event<>();
        event.index = index;
        event.type = type;
        event.payload = payload;
        event.arg1 = arg1;
        event.arg2 = arg2;
        event.consumer = consumer;
        q.offer((Event<Object>) event);
    }

    @Override
    protected void runInThread() {
        Thread thread = Thread.currentThread();
        while (!thread.isInterrupted()) {
            Event<Object> event = q.poll();
            if (event != null) {
                Consumer<Object> consumer = event.consumer;
                consumer = null;
                try {
                    consumer.accept(event);
                } catch (Exception e) {
                    LOGGER.error("error", e);
                }
            } else {
                waitForMore();
            }
        }
    }
}
