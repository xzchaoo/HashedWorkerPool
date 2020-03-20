package com.xzchaoo.eventloop;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
@FunctionalInterface
public interface Consumer<P> {
    /**
     * consume event
     *
     * @param event event
     */
    void accept(Event<P> event);
}
