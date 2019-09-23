package com.xzchaoo.hashedworkerpool.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

import org.junit.Test;

/**
 * @author xzchaoo
 * @date 2019-09-18
 */
public class HashedWorkerPoolImplTest {
    @Test
    public void test() throws InterruptedException {
        HashedWorkerPoolImpl pool = new HashedWorkerPoolImpl(
                1024,
                1,
                new ThreadFactoryBuilder().setNameFormat("test-%d").build(),
                ProducerType.MULTI,
                new BlockingWaitStrategy());
        pool.start();
        pool.broadcast("asdf", new Consumer<String>() {
            @Override
            public void consume(int hash, int index, String payload) {
                System.out.println(payload);
            }
        });
        pool.publish(1, "foo", new Consumer<String>() {
            @Override
            public void consume(int hash, int index, String payload) {
                System.out.println(payload);
            }
        });
        Thread.sleep(1000);
        pool.stop(true);
    }
}
