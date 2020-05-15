package com.xzchaoo.eventloop;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * created at 2020/5/16
 *
 * @author xzchaoo
 */
public final class TypeIndex {
    private static final AtomicInteger INDEX = new AtomicInteger();

    private TypeIndex() {
    }

    public static int next() {
        return INDEX.incrementAndGet();
    }
}
