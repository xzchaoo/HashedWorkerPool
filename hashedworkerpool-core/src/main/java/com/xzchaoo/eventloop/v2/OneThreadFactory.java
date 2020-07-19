package com.xzchaoo.eventloop.v2;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
public class OneThreadFactory implements ThreadFactory {
    private final String name;

    public OneThreadFactory(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, name);
    }
}
