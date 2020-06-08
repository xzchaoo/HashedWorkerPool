package com.xzchaoo.eventloop.batchprocessor;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * created at 2020/3/21
 *
 * @author xzchaoo
 */
public interface Flusher<T> {
    void onMissingSemaphore(List<T> buffer, Semaphore semaphore, Context<T> ctx);

    /**
     * 取得信号量, 并要求刷新
     *
     * @param buffer
     * @param ctx
     */
    void flush(List<T> buffer, Context<T> ctx);

    interface Context<T> {
        void complete();

        void retry(Throwable e, long delayMills, List<T> buffer);

        void error(Throwable e);
    }
}
