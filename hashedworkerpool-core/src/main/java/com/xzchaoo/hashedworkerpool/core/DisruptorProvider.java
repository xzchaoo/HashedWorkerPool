package com.xzchaoo.hashedworkerpool.core;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * @author xzchaoo
 */
public interface DisruptorProvider {
    Disruptor<EventHolder> provide(EventFactory<EventHolder> factory);
}
