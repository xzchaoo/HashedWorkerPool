package com.xzchaoo.hashedworkerpool.ext;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * created at 2020/3/20
 *
 * @author xzchaoo
 */
public abstract class AbstractEventLoopManager implements EventLoopManager {
    private static final int STATE_NOT_INIT = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_STOPPED = 2;
    protected final int size;
    protected final int mask;
    private volatile int state = STATE_NOT_INIT;
    private final ScheduledThreadPoolExecutor scheduler;
    private static final AtomicIntegerFieldUpdater<AbstractEventLoopManager> UPDATER =
        AtomicIntegerFieldUpdater.newUpdater(AbstractEventLoopManager.class, "state");
    protected final String name;

    public AbstractEventLoopManager(ManagerProperties p) {
        this.name = p.getName();
        // TODO 2^n
        this.size = p.getSize();
        this.mask = size - 1;
        ThreadFactory globalSchedulerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat(name + "-GlobalScheduler-%d")
            .build();
        scheduler = new ScheduledThreadPoolExecutor(p.getGlobalSchedulerSize(), globalSchedulerThreadFactory);
    }

    @Override
    public final <P> void publish(int hash, P payload, Consumer<P> consumer) {
        publish(hash, 0, payload, null, null, consumer);
    }

    @Override
    public final <P> void publish(int hash, P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        publish(hash, 0, payload, arg1, arg2, consumer);
    }

    @Override
    public final <P> void publish(int hash, int type, P payload) {
        publish(hash, 0, payload, null, null);
    }

    @Override
    public final <P> void publish(int hash, int type, P payload, Object arg1, Object arg2) {
        int index = hash & mask;
        eventLoop(index).publish(type, payload, arg1, arg2);
    }

    protected abstract <P> void publish(int hash, int type, P payload, Object arg1, Object arg2, Consumer<P> consumer);

    protected abstract AbstractEventLoop eventLoop(int index);

    @Override
    public final void start() {
        int state = this.state;
        if (state == STATE_NOT_INIT) {
            if (UPDATER.compareAndSet(this, state, STATE_RUNNING)) {
                doStart();
            }
        } else {
            if (state == STATE_STOPPED) {
                throw new IllegalStateException("Already stopped");
            }
            // else already started
        }
    }

    protected abstract void doStart();

    @Override
    public final void stop() {
        int state = this.state;
        if (state == STATE_RUNNING) {
            if (UPDATER.compareAndSet(this, state, STATE_STOPPED)) {
                scheduler.shutdownNow();
                doStop();
            }
        } else {
            if (state == STATE_NOT_INIT) {
                throw new IllegalStateException("Not started");
            }
            // else already stopped
        }
    }

    protected abstract void doStop();

    @Override
    public final ScheduledExecutorService globalScheduler() {
        return scheduler;
    }

    @Override
    public final int size() {
        return size;
    }

    /**
     * @param type
     * @param consumer 必须是线程安全的实现
     * @param <P>
     */
    public final <P> void register(int type, Consumer<P> consumer) {
        for (int i = 0; i < size; i++) {
            eventLoop(i).register(type, consumer);
        }
    }

    public final <P> void register(int type, ConsumerFactory<P> factory) {
        for (int i = 0; i < size; i++) {
            eventLoop(i).register(type, factory);
        }
    }
}
