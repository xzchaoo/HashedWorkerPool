package com.xzchaoo.eventloop.v2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
@SuppressWarnings("WeakerAccess")
public final class TypeIndex {
    private static final AtomicInteger next = new AtomicInteger();

    private TypeIndex() {
    }

    public static int next() {
        return next.incrementAndGet();
    }
}
