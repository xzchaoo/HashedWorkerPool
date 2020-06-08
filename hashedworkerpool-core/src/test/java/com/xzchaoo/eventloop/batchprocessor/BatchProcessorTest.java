package com.xzchaoo.eventloop.batchprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.junit.Test;

import com.xzchaoo.eventloop.AbstractEventLoopManager;
import com.xzchaoo.eventloop.EventLoopManagerConfig;
import com.xzchaoo.eventloop.disruptor.DisruptorEventLoopManager;

/**
 * created at 2020/3/21
 *
 * @author xzchaoo
 */
public class BatchProcessorTest {
    @Test
    public void test() throws InterruptedException {
        EventLoopManagerConfig p = new EventLoopManagerConfig();
        p.setSize(1);
        p.setName("test");
        AbstractEventLoopManager manager = new DisruptorEventLoopManager(p);
        manager.start();
        BatchProcessor<String> batchProcessor = new BatchProcessor<>(manager, MyFlusher::new);
        batchProcessor.start();
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        for (int i = 0; i < 1000; i++) {
            batchProcessor.put(list);
        }
        Thread.sleep(2000);
    }

    private static class MyFlusher implements Flusher<String> {
        public MyFlusher(int index) {
        }

        @Override
        public void onMissingSemaphore(List<String> buffer, Semaphore semaphore, Context<String> ctx) {
            System.out.println("丢弃数据 " + buffer.size());
        }

        @Override
        public void flush(List<String> buffer, Context<String> ctx) {
            try {
                System.out.println(buffer.size());
            } finally {
                ctx.complete();
            }
        }
    }
}
