package com.xzchaoo.hashedworkerpool.core;

import com.lmax.disruptor.dsl.Disruptor;

/**
 * 是一个可以保证hash值相同的任务分到同一个线程上处理的固定大小的线程池. 注意每个任务最好都是非常快的计算任务, 否则可能会导致积压.
 *
 * @author xzchaoo
 * @date 2019/7/23
 */
public interface HashedWorkerPool {
    /**
     * 启动
     */
    void start();

    /**
     * 停止
     *
     * @param waitAllToFinish 是否等待积压队列里的所有任务都被处理完才停止
     */
    void stop(boolean waitAllToFinish);

    /**
     * 发布一个工作项(本身可以提供hash信息)到线程池
     *
     * @param payload  负载
     * @param consumer 消费者
     * @param <P>      泛型
     */
    <P extends PartitionKey> void publish(P payload, Consumer<P> consumer);

    /**
     * 发布一个工作项(显示提供hash值)到线程池
     *
     * @param hash     哈希值
     * @param payload  负载
     * @param consumer 消费者
     * @param <P>      泛型
     */
    <P> void publish(int hash, P payload, Consumer<P> consumer);

    /**
     * 广播所有线程. 将会收到 {@code size()} 次回调
     *
     * @param payload  负载
     * @param consumer
     */
    <P> void broadcast(P payload, Consumer<P> consumer);

    /**
     * 该线程池大小
     *
     * @return
     */
    int size();

    Disruptor<?> getDisruptor();
}
