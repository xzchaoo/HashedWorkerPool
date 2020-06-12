package com.xzchaoo.hashedworkerpool.core;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ThreadFactory;

/**
 * @author xzchaoo
 */
@SuppressWarnings({"unchecked", "FieldCanBeLocal"})
public class HashedWorkerPoolImpl implements HashedWorkerPool {
    private final EventFactory<EventHolder> eventFactory = EventHolder::new;

    private final int                    threadCount;
    private final Disruptor<EventHolder> disruptor;

    private RingBuffer<EventHolder> ringBuffer;

    public HashedWorkerPoolImpl(int bufferSize, int threadCount, ThreadFactory threadFactory, ProducerType producerType
            , WaitStrategy waitStrategy) {
        this(bufferSize, threadCount, threadFactory, producerType, waitStrategy, new LoggingExceptionHandler());
    }

    public HashedWorkerPoolImpl(int bufferSize, int threadCount, ThreadFactory threadFactory, ProducerType producerType
            , WaitStrategy waitStrategy, ExceptionHandler<EventHolder> exceptionHandler) {
        this(threadCount, factory -> {
            Disruptor<EventHolder> d = new Disruptor<>(factory, bufferSize, threadFactory,
                    producerType, waitStrategy);
            if (exceptionHandler != null) {
                d.setDefaultExceptionHandler(exceptionHandler);
            }
            return d;
        });
    }

    public HashedWorkerPoolImpl(int threadCount, DisruptorProvider provider) {
        this.threadCount = threadCount;
        disruptor = provider.provide(eventFactory);
        for (int i = 0; i < threadCount; ++i) {
            disruptor.handleEventsWith(new Worker(i));
        }
        ringBuffer = disruptor.getRingBuffer();
    }

    @Override
    public void start() {
        disruptor.start();
    }

    @Override
    public void stop(boolean waitAllToFinish) {
        // shutdown halt区别 是:
        // 1. shutdown 会先等待没数据才尝试stop (但是理论上 判断没数据 和 stop 两个操作并不是原子的, 中间可能混进来一些数据...)
        // 2. shutdown 要注意一个问题, 它并没有阻止数据写入, 此时数据依旧可以继续写入!
        // 3. halt 直接停止底层processor, 此时队列里可能有未处理完的数据
        if (waitAllToFinish) {
            disruptor.shutdown();
        } else {
            disruptor.halt();
        }
    }

    @Override
    public <P extends PartitionKey> void publish(P payload, Consumer<P> consumer) {
        int hash = payload.getHash();
        publish(hash, payload, consumer);
    }

    @Override
    public <P> void publish(int hash, P payload, Consumer<P> consumer) {
        int index = (hash & 0X7FFF_FFFF) % threadCount;
        doPublish(hash, index, payload, consumer);
    }

    @Override
    public <P> void broadcast(P payload, Consumer<P> consumer) {
        for (int index = 0; index < threadCount; index++) {
            doPublish(0, index, payload, consumer);
        }
    }

    private <P> void doPublish(int hash, int index, P payload, Consumer<P> consumer) {
        long next = ringBuffer.next();
        EventHolder holder = ringBuffer.get(next);
        holder.hash = hash;
        holder.index = index;
        holder.payload = payload;
        holder.consumer = consumer;
        ringBuffer.publish(next);
    }

    @Override
    public int size() {
        return threadCount;
    }

    @Override
    public Disruptor<?> getDisruptor() {
        return disruptor;
    }
}
