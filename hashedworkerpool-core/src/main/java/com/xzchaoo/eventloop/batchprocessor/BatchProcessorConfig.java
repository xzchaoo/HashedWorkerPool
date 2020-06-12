package com.xzchaoo.eventloop.batchprocessor;

import java.time.Duration;

/**
 * created at 2020/5/16
 *
 * @author xzchaoo
 */
public class BatchProcessorConfig {
    private int bufferSize = 1024;
    private Duration forceFlushInterval = Duration.ofSeconds(5);
    private int concurrency = 16;
    private boolean blocking = true;
    private int maxRetryCount = 1;

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Duration getForceFlushInterval() {
        return forceFlushInterval;
    }

    public void setForceFlushInterval(Duration forceFlushInterval) {
        this.forceFlushInterval = forceFlushInterval;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }
}
