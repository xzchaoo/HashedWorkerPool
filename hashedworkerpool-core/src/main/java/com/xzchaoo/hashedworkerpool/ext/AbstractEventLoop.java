package com.xzchaoo.hashedworkerpool.ext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * created at 2020/3/20
 *
 * @author xzchaoo
 */
public abstract class AbstractEventLoop implements EventLoop {
    private static final int STATE_NOT_INIT = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_STOPPED = 2;
    protected final String name;
    protected final int index;
    private final EventLoopManager manager;
    private volatile int state = STATE_NOT_INIT;
    private static final AtomicIntegerFieldUpdater<AbstractEventLoop> UPDATER =
        AtomicIntegerFieldUpdater.newUpdater(AbstractEventLoop.class, "state");
    private final Map<Integer, Consumer<?>> processMap = new HashMap<>();
    private final ScheduledExecutorService scheduler;

    public AbstractEventLoop(String name, int index, EventLoopManager manager) {
        this.name = name;
        this.index = index;
        this.manager = manager;
        ThreadFactory schedulerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat(name + "-Scheduler")
            .build();
        scheduler = new ScheduledThreadPoolExecutor(1, schedulerThreadFactory);
    }

    @Override
    public final EventLoopManager manager() {
        return manager;
    }

    private void publish(Consumer<Void> consumer) {
        publish(0, null, null, null, consumer);
    }

    @Override
    public final <P> void publish(P payload, Consumer<P> consumer) {
        publish(0, payload, null, null, consumer);
    }

    @Override
    public final <P> void publish(P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        publish(0, payload, arg1, arg2, consumer);
    }

    @Override
    public final <P> void publish(int type, P payload) {
        publish(type, payload, null, null);
    }

    @Override
    public final <P> void publish(int type, P payload, Object arg1, Object arg2) {
        Consumer<P> consumer = (Consumer<P>) processMap.get(type);
        if (consumer == null) {
            return;
        }
        publish(type, payload, arg1, arg2, consumer);
    }


    protected abstract <P> void publish(int type, P payload, Object arg1, Object arg2, Consumer<P> consumer);

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
    public final ScheduledExecutorService scheduler() {
        return scheduler;
    }

    @Override
    public final ScheduledExecutorService globalScheduler() {
        return manager.globalScheduler();
    }


    @Override
    public final ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
        return scheduler.schedule(() -> publish(event -> runnable.run()), delay, unit);
    }

    @Override
    public final ScheduledFuture<?> schedule(java.util.function.Consumer<EventLoop> consumer, long delay,
                                             TimeUnit unit) {
        return scheduler.schedule(() -> publish(event -> consumer.accept(this)), delay, unit);
    }

    @Override
    public final ScheduledFuture<?> scheduledWithFixedDelay(Runnable runnable, long delay, long period, TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(() -> publish(event -> runnable.run()), delay, period, unit);
    }

    @Override
    public final ScheduledFuture<?> scheduledWithFixedDelay(java.util.function.Consumer<EventLoop> consumer, long delay,
                                                            long period, TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(() -> publish(event -> consumer.accept(this)), delay, period, unit);
    }

    /**
     * @param type
     * @param consumer 必须是线程安全的实现
     * @param <P>
     */
    public final <P> void register(int type, Consumer<P> consumer) {
        if (processMap.containsKey(type)) {
            throw new IllegalStateException("Duplicated type " + type);
        }
        processMap.put(type, consumer);
    }

    public final <P> void register(int type, ConsumerFactory<P> factory) {
        if (processMap.containsKey(type)) {
            throw new IllegalStateException("Duplicated type " + type);
        }
        processMap.put(type, factory.create(index));
    }
}
