package com.xzchaoo.eventloop.batchprocessor;

import com.xzchaoo.eventloop.EventLoopManager;
import com.xzchaoo.eventloop.EventLoopManagerConfig;
import com.xzchaoo.eventloop.disruptor.DisruptorEventLoopManager;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * created at 2020/3/21
 *
 * @author xzchaoo
 */
public class BatchProcessorTest {
    @Test
    public void test() throws InterruptedException {
        EventLoopManagerConfig p = new EventLoopManagerConfig();
        p.setSize(4);
        p.setName("test");
        EventLoopManager manager = new DisruptorEventLoopManager(p);
        manager.start();

        BatchProcessorConfig bpc = new BatchProcessorConfig();
        bpc.setBufferSize(100);
        BatchProcessor<String> batchProcessor = new BatchProcessor<>(manager, bpc,
            MyFlusher::new);
        batchProcessor.start();
        for (int i = 0; i < 1000; i++) {
            batchProcessor.put("1");
            batchProcessor.put("2");
            batchProcessor.put("3");
            batchProcessor.put("4");
        }
        Thread.sleep(2000);
    }

    private static class MyFlusher implements Flusher<String> {
        public MyFlusher(int index) {
        }

        @Override
        public void onMissingSemaphore(List<String> buffer, Context ctx) {
            System.out.println(buffer.size());
        }

        @Override
        public void flush(List<String> buffer, Context ctx) {
            try {
                System.out.println("flush " + buffer.size());
            } finally {
                ctx.complete();
            }
        }
    }
}