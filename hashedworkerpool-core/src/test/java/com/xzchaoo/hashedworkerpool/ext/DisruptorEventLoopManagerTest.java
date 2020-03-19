package com.xzchaoo.hashedworkerpool.ext;

import com.xzchaoo.hashedworkerpool.ext.disruptor.DisruptorEventLoopManager;

import org.junit.Test;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
public class DisruptorEventLoopManagerTest {
    @Test
    public void test() throws InterruptedException {
        ManagerProperties p = new ManagerProperties();
        p.setSize(4);
        p.setName("test");
        DisruptorEventLoopManager m = new DisruptorEventLoopManager(p);
        m.register(1, new ConsumerFactory<String>() {
            @Override
            public Consumer<String> create(int index) {
                return new Consumer<String>() {
                    @Override
                    public void accept(Event<String> event) {
                        System.out.println("在线程 " + index + " 处理事件" + event);
                    }
                };
            }
        });
        m.start();
        for (int i = 0; i < 10; i++) {
            m.publish(i, 1, "", "", "");
        }
        Thread.sleep(1000);
    }
}