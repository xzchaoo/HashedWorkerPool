package com.xzchaoo.eventloop.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
public class StringProcessor implements Consumer<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringProcessor.class);

    @Override
    public void consume(EventLoop eventLoop, String payload) {
        LOGGER.info("consume {} in {}", payload, eventLoop.index());
    }
}
