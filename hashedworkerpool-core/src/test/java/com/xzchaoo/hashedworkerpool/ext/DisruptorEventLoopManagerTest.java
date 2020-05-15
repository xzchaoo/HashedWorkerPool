package com.xzchaoo.hashedworkerpool.ext;

import org.junit.Test;

import com.xzchaoo.eventloop.AbstractEventLoopManager;
import com.xzchaoo.eventloop.Consumer;
import com.xzchaoo.eventloop.ConsumerFactory;
import com.xzchaoo.eventloop.Event;
import com.xzchaoo.eventloop.EventLoop;
import com.xzchaoo.eventloop.EventLoopManagerConfig;
import com.xzchaoo.eventloop.disruptor.DisruptorEventLoopManager;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
public class DisruptorEventLoopManagerTest {
    @Test
    public void test() throws InterruptedException {
        EventLoopManagerConfig p = new EventLoopManagerConfig();
        p.setSize(4);
        p.setName("test");
        // AbstractEventLoopManager m = new ThreadPoolEventLoopManager(p);
        AbstractEventLoopManager m = new DisruptorEventLoopManager(p);
        m.register2(1, new ConsumerFactory<String>() {
            @Override
            public Consumer<String> create(EventLoop eventLoop) {
                return new Consumer<String>() {
                    @Override
                    public void accept(Event<String> event) {
                        System.out.println("在线程 " + Thread.currentThread() + " " + eventLoop.index() + " 处理事件" + event);
                    }
                };
            }
        });
        m.start();
        for (int i = 0; i < 10; i++) {
            m.publish2(i, 1, "", "", "");
        }
        Thread.sleep(1000);
    }
}
