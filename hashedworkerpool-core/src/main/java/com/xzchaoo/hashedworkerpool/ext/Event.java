package com.xzchaoo.hashedworkerpool.ext;

import lombok.Getter;
import lombok.ToString;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
@ToString
public class Event<P> {
    @Getter
    public int index;
    @Getter
    public int type;
    @Getter
    public P payload;
    @Getter
    public Object arg1;
    @Getter
    public Object arg2;
    public Consumer<P> consumer;
}
