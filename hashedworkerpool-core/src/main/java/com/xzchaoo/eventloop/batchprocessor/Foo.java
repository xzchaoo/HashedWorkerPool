package com.xzchaoo.eventloop.batchprocessor;

import java.util.List;

/**
 * created at 2020/3/21
 *
 * @author xzchaoo
 */
public interface Foo<T> {
    void onMissingSemaphore(int index, List<T> buffer);
}
