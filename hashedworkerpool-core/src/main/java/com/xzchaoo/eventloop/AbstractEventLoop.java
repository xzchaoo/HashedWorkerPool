package com.xzchaoo.eventloop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * created at 2020/3/20
 *
 * @author xzchaoo
 */
public abstract class AbstractEventLoop implements EventLoop {
    protected static final AtomicIntegerFieldUpdater<AbstractEventLoop> UPDATER =
        AtomicIntegerFieldUpdater.newUpdater(AbstractEventLoop.class, "state");

    protected static final int STATE_NOT_INIT = 0;
    protected static final int STATE_RUNNING = 1;
    protected static final int STATE_STOPPED = 2;
    protected final String name;
    protected final int index;
    protected final EventLoopManager manager;
    protected volatile int state = STATE_NOT_INIT;
    protected final ScheduledExecutorService scheduler;
    protected volatile Thread eventLoopThread;

    private volatile Map<Object, Consumer<?>> processMap = new HashMap<>();
    private static final AtomicReferenceFieldUpdater<AbstractEventLoop, Map> PROCESS_MAP_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(AbstractEventLoop.class, Map.class, "processMap");

    public AbstractEventLoop(EventLoopConfig config, EventLoopManager manager) {
        this.name = config.getName();
        this.index = config.getIndex();
        this.manager = manager;
        SingleThreadFactory threadFactory = config.getEventLoopThreadFactory();
        threadFactory.setNotify(this::setEventLoopThread);
        ThreadFactory schedulerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat(name + "-Scheduler")
            .build();
        scheduler = new ScheduledThreadPoolExecutor(1, schedulerThreadFactory);
    }

    @Override
    public final EventLoopManager manager() {
        return manager;
    }

    private void publish0(Consumer<Void> consumer) {
        publish0(0, null, null, null, consumer);
    }

    @Override
    public final <P> void publish1(P payload, Consumer<P> consumer) {
        publish0(0, payload, null, null, consumer);
    }

    @Override
    public <P> void publish1(P payload, Object arg1, Consumer<P> consumer) {
        publish0(0, payload, arg1, null, consumer);
    }

    @Override
    public final <P> void publish1(P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        publish0(0, payload, arg1, arg2, consumer);
    }

    @Override
    public final <P> void publish2(Object type, P payload) {
        publish2(type, payload, null, null);
    }

    @Override
    public <P> void publish2(Object type, P payload, Object arg1) {
        publish2(type, payload, arg1, null);
    }

    @Override
    public final <P> void publish2(Object type, P payload, Object arg1, Object arg2) {
        Consumer<P> consumer = (Consumer<P>) processMap.get(type);
        if (consumer == null) {
            return;
        }
        publish0(type, payload, arg1, arg2, consumer);
    }

    protected abstract <P> void publish0(Object type, P payload, Object arg1, Object arg2, Consumer<P> consumer);

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
        return scheduler.schedule(() -> publish0(event -> runnable.run()), delay, unit);
    }

    @Override
    public final ScheduledFuture<?> schedule(java.util.function.Consumer<EventLoop> consumer, long delay,
                                             TimeUnit unit) {
        return scheduler.schedule(() -> publish0(event -> consumer.accept(this)), delay, unit);
    }

    @Override
    public final ScheduledFuture<?> scheduledWithFixedDelay(Runnable runnable, long delay, long period, TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(() -> publish0(event -> runnable.run()), delay, period, unit);
    }

    @Override
    public final ScheduledFuture<?> scheduledWithFixedDelay(java.util.function.Consumer<EventLoop> consumer, long delay,
                                                            long period, TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(() -> publish0(event -> consumer.accept(this)), delay, period, unit);
    }

    /**
     * @param type
     * @param consumer 必须是线程安全的实现
     * @param <P>
     */
    public final <P> void register(Object type, Consumer<P> consumer) {
        for (; ; ) {
            // PROCESS_MAP_UPDATER
            Map<Object, Consumer<?>> processMap = this.processMap;
            if (processMap.containsKey(type)) {
                throw new IllegalStateException("Duplicated type " + type);
            }
            Map<Object, Consumer<?>> newProcessMap = new HashMap<>(this.processMap);
            newProcessMap.put(type, consumer);
            if (PROCESS_MAP_UPDATER.compareAndSet(this, processMap, newProcessMap)) {
                break;
            }
        }
    }

    @Override
    public final <P> void register(Object type, ConsumerFactory<P> factory) {
        Consumer<P> consumer = null;
        for (; ; ) {
            // PROCESS_MAP_UPDATER
            Map<Object, Consumer<?>> processMap = this.processMap;
            if (processMap.containsKey(type)) {
                throw new IllegalStateException("Duplicated type " + type);
            }
            Map<Object, Consumer<?>> newProcessMap = new HashMap<>(this.processMap);
            if (consumer == null) {
                consumer = factory.create(this);
            }
            newProcessMap.put(type, consumer);
            if (PROCESS_MAP_UPDATER.compareAndSet(this, processMap, newProcessMap)) {
                break;
            }
        }
    }

    @Override
    public boolean inEventLoop() {
        return eventLoopThread == Thread.currentThread();
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return this.eventLoopThread == thread;
    }

    protected void setEventLoopThread(Thread eventLoopThread) {
        this.eventLoopThread = eventLoopThread;
    }

    @Override
    public void batchPublish(BatchPublish batch) {
        if (batch == null) {
            return;
        }
        List<BatchPublish.Event<?>> events = batch.getEvents();
        if (events.isEmpty()) {
            return;
        }
        for (BatchPublish.Event<?> event : events) {
            Consumer<Object> consumer = (Consumer<Object>) event.consumer;
            if (consumer == null) {
                consumer = (Consumer<Object>) processMap.get(event.type);
            }
            if (consumer == null) {
                continue;
            }
            publish0(event.type, event.payload, event.arg1, event.arg2, consumer);
        }
    }

    @Override
    public int index() {
        return index;
    }
}
