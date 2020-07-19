package com.xzchaoo.eventloop.v2;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
public class Event<P> {
    P payload;
    Consumer<P> consumer;
}
