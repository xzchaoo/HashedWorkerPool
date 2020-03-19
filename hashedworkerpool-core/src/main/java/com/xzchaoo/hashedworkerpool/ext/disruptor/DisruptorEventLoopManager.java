package com.xzchaoo.hashedworkerpool.ext.disruptor;

import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.xzchaoo.hashedworkerpool.ext.AbstractEventLoopManager;
import com.xzchaoo.hashedworkerpool.ext.Consumer;
import com.xzchaoo.hashedworkerpool.ext.Event;
import com.xzchaoo.hashedworkerpool.ext.EventLoop;
import com.xzchaoo.hashedworkerpool.ext.ManagerProperties;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
public class DisruptorEventLoopManager extends AbstractEventLoopManager {
    private final DisruptorEventLoop[] eventLoops;

    public DisruptorEventLoopManager(ManagerProperties p) {
        super(p);
        this.eventLoops = new DisruptorEventLoop[size];
        LiteBlockingWaitStrategy waitStrategy = new LiteBlockingWaitStrategy();
        int ringBufferSize = p.getRingBufferSize();
        for (int i = 0; i < size; i++) {
            String eventLoopName = name + "-" + i;
            this.eventLoops[i] = new DisruptorEventLoop(eventLoopName, i, ringBufferSize, waitStrategy, this);
        }
    }

    @Override
    public final <P> void publish(int hash, Event<P> event) {
        int index = hash & mask;
        eventLoops[index].publish(event);
    }

    @Override
    protected final <P> void publish(int hash, int type, P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        int index = hash & mask;
        eventLoops[index].publish(type, payload, arg1, arg2, consumer);
    }

    @Override
    protected final DisruptorEventLoop eventLoop(int index) {
        return eventLoops[index];
    }

    @Override
    protected final void doStart() {
        for (EventLoop eventLoop : eventLoops) {
            eventLoop.start();
        }
    }

    @Override
    protected final void doStop() {
        for (EventLoop eventLoop : eventLoops) {
            eventLoop.stop();
        }
    }

}
