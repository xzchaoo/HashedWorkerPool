package com.xzchaoo.hashedworkerpool.ext;

/**
 * consumer factory
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
@FunctionalInterface
public interface ConsumerFactory<P> {
    /**
     * create consumer for event loop
     *
     * @param index event loop index
     * @return consumer for that event loop
     */
    Consumer<P> create(int index);
}
