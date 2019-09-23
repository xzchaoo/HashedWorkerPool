package com.xzchaoo.hashedworkerpool.core;

import com.lmax.disruptor.ExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xzchaoo
 * @date 2019/7/25
 */
public class LoggingExceptionHandler implements ExceptionHandler<EventHolder> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingExceptionHandler.class);

    @Override
    public void handleEventException(Throwable ex, long sequence, EventHolder event) {
        LOGGER.error("handleEventException", ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        LOGGER.error("handleOnStartException", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        LOGGER.error("handleOnShutdownException", ex);
    }
}
