package com.xzchaoo.eventloop.batchprocessor;

import com.xzchaoo.eventloop.EventLoop;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * created at 2020/3/21
 *
 * @author xzchaoo
 */
public interface Flusher<T> {
    void onMissingSemaphore(List<T> buffer, Context ctx);

    void flush(List<T> buffer, Context ctx);

    interface Context<T> {
        void complete();

        EventLoop eventLoop();

        Semaphore semaphore();

        void retry(long delayMills, List<T> buffer);

        int getMaxRetryCount();

        int getRetryCount();
    }
}
