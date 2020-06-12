package com.xzchaoo.hashedworkerpool.core;

/**
 * @author xiangfeng.xzc
 */
class Worker implements com.lmax.disruptor.EventHandler<EventHolder> {
    private final int workerIndex;

    Worker(int workerIndex) {
        this.workerIndex = workerIndex;
    }

    @Override
    public void onEvent(EventHolder holder, long sequence, boolean endOfBatch) throws Exception {
        if (workerIndex != holder.index) {
            return;
        }
        try {
            holder.consumer.consume(holder.hash, holder.index, holder.payload);
        } finally {
            holder.reset();
        }
    }
}
