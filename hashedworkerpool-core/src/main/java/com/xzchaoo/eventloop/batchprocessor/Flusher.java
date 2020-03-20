package com.xzchaoo.eventloop.batchprocessor;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * created at 2020/3/21
 *
 * @author xzchaoo
 */
public interface Flusher<T> {
    void onMissingSemaphore(List<T> buffer, Semaphore semaphore);

    void flush(List<T> buffer, Context ctx);

    interface Context<T> {
        void complete();

        void retry(Throwable e, long delayMills, List<T> buffer);

        void error(Throwable e);
    }
}
