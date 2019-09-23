package com.xzchaoo.hashedworkerpool.core;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * @author xzchaoo
 * @date 2019/7/23
 */
public interface DisruptorProvider {
    Disruptor<EventHolder> provide(EventFactory<EventHolder> factory);
}
