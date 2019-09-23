package com.xzchaoo.hashedworkerpool.core;

/**
 * @author xiangfeng.xzc
 * @date 2019-09-11
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
        holder.consumer.consume(holder.hash, holder.index, holder.payload);
    }
}
