package com.xzchaoo.eventloop.v2;

import com.lmax.disruptor.ExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
public class LogAndIgnoreExceptionHandler implements ExceptionHandler<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAndIgnoreExceptionHandler.class);

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
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
