package com.xzchaoo.hashedworkerpool.core;

/**
 * 消费者接口
 *
 * @author xzchaoo
 * @date 2019/7/23
 */
@FunctionalInterface
public interface Consumer<P> {

    /**
     * 回调接口
     *
     * @param hash    hash值
     * @param index   工作者索引
     * @param payload 负载
     */
    void consume(int hash, int index, P payload);
}
