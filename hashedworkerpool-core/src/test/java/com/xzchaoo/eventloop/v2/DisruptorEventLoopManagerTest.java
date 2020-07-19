package com.xzchaoo.eventloop.v2;

import org.junit.Test;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
public class DisruptorEventLoopManagerTest {
    @Test
    public void test() {
        EventLoopManagerConfig c = new EventLoopManagerConfig();
        c.setName("foo");
        c.setBufferSize(65536);
        DisruptorEventLoopManager m = new DisruptorEventLoopManager(c);
        m.start();
        int t1 = TypeIndex.next();
        m.register(t1, el -> new StringProcessor());
        for (int i = 0; i < 100; i++) {
            m.publish(i, t1, Integer.toString(i));
        }
    }
}