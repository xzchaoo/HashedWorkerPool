package com.xzchaoo.eventloop.concurrency;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.xzchaoo.eventloop.Consumer;
import com.xzchaoo.eventloop.Event;
import com.xzchaoo.eventloop.EventLoopConfig;
import com.xzchaoo.eventloop.EventLoopManager;
import com.xzchaoo.eventloop.SingleThreadFactory;

/**
 * @author xiangfeng.xzc
 */
public class ConcurrencyEventLoop extends SelfManageThreadEventLoop {

    private final ConcurrentLinkedQueue<Event<?>> q;

    public ConcurrencyEventLoop(EventLoopConfig config, EventLoopManager manager, SingleThreadFactory threadFactory) {
        super(config, manager);
        q = new ConcurrentLinkedQueue<>();
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
        q.offer(event);
        notifyMore();
    }

    @Override
    protected void runInThread() {
        Thread thread = Thread.currentThread();
        this.eventLoopThread = thread;
        while (!thread.isInterrupted()) {
            Event<?> e = q.poll();
            if (e != null) {
                process(e);
            } else {
                waitForMore();
            }
        }
    }

    private void process(Event<?> e) {
        Consumer<?> consumer = e.consumer;
        e.consumer = null;
        consumer.accept((Event) e);
    }

}
