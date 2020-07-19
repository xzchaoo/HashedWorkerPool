package com.xzchaoo.eventloop.v2;

import com.lmax.disruptor.LiteBlockingWaitStrategy;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
public class DisruptorEventLoopManager implements EventLoopManager {
    final EventLoopManagerConfig config;
    private DisruptorEventLoop[] eventLoops;
    private final int size;

    private final AtomicInteger state = new AtomicInteger(STATE_NEW);
    static final int STATE_NEW = 0;
    static final int STATE_STARTING = 1;
    static final int STATE_STARTED = 2;
    static final int STATE_STOPPED = 3;
    static final String[] STATES = {"NEW", "STARTING", "STARTED", "STOPPED"};

    public DisruptorEventLoopManager(EventLoopManagerConfig config) {
        this.config = Objects.requireNonNull(config);
        String name = config.getName();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }
        int bufferSize = config.getBufferSize();
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize <= 0");
        }
        int size = config.getSize();
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }

        this.size = size;
        this.eventLoops = new DisruptorEventLoop[this.size];
        for (int i = 0; i < this.size; i++) {
            this.eventLoops[i] = new DisruptorEventLoop(this, //
                i, //
                new LiteBlockingWaitStrategy() //
            );
        }
    }

    @Override
    public void start() {
        int state = this.state.get();
        if (state == STATE_NEW && this.state.compareAndSet(state, STATE_STARTING)) {
            for (DisruptorEventLoop eventLoop : eventLoops) {
                eventLoop.start();
            }
            return;
        }
        state = this.state.get();
        throw new IllegalStateException("invalid state " + STATES[state] + " for start");
    }

    @Override
    public void stop() {
        int state = this.state.get();
        if (state == STATE_STARTED && this.state.compareAndSet(state, STATE_STOPPED)) {
            for (DisruptorEventLoop eventLoop : eventLoops) {
                eventLoop.stop();
            }
            return;
        }
        state = this.state.get();
        throw new IllegalStateException("invalid state " + STATES[state] + " for stop");
    }

    @Override
    public DisruptorEventLoop eventLoop(int index) {
        return eventLoops[index];
    }

    @Override
    public DisruptorEventLoop eventLoopByHash(int hash) {
        return eventLoops[index(hash)];
    }

    @Override
    public <P> void publish(int hash, P payload, Consumer<P> consumer) {
        eventLoopByHash(hash).publish(payload, consumer);
    }

    @Override
    public <P> void broadcast(P payload, Consumer<P> consumer) {
        for (DisruptorEventLoop eventLoop : eventLoops) {
            eventLoop.publish(payload, consumer);
        }
    }

    @Override
    public <P> void publish(int hash, int type, P payload) {
        eventLoopByHash(hash).publish(type, payload);
    }

    @Override
    public <P> void broadcast(int type, P payload) {
        for (DisruptorEventLoop eventLoop : eventLoops) {
            eventLoop.publish(type, payload);
        }
    }

    @Override
    public <P> void register(int type, TypeConsumerFactory<P> factory) {
        for (DisruptorEventLoop eventLoop : eventLoops) {
            Consumer<P> consumer = factory.create(eventLoop);
            eventLoop.registry(type, consumer);
        }
    }

    @Override
    public int size() {
        return size;
    }

    private int index(int hash) {
        return (hash & 0X7FFF_FFFF) % size;
    }
}
