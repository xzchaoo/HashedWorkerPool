package com.xzchaoo.eventloop.disruptor;

import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.xzchaoo.eventloop.AbstractEventLoop;
import com.xzchaoo.eventloop.AbstractEventLoopManager;
import com.xzchaoo.eventloop.EventLoopConfig;
import com.xzchaoo.eventloop.EventLoopManagerConfig;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
public class DisruptorEventLoopManager extends AbstractEventLoopManager {

    private final LiteBlockingWaitStrategy waitStrategy;

    public DisruptorEventLoopManager(EventLoopManagerConfig p) {
        super(p);
        waitStrategy = new LiteBlockingWaitStrategy();
        initEventLoop();
    }

    @Override
    protected AbstractEventLoop createEventLoop(EventLoopConfig config) {
        return new DisruptorEventLoop(config.getName(), config.getIndex(), config.getManagerConfig().getEventLoopBufferSize(),
                waitStrategy, this, config.getEventLoopThreadFactory());
    }

    @Override
    protected final DisruptorEventLoop eventLoop(int index) {
        return (DisruptorEventLoop) super.eventLoop(index);
    }

}
