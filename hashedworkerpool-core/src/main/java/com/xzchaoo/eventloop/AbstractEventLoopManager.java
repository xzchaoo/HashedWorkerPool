package com.xzchaoo.eventloop;

import java.util.List;
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
    private static final AtomicIntegerFieldUpdater<AbstractEventLoopManager> UPDATER =
        AtomicIntegerFieldUpdater.newUpdater(AbstractEventLoopManager.class, "state");

    private static final int STATE_NOT_INIT = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_STOPPED = 2;
    protected final int size;
    protected final int mask;
    protected final EventLoopManagerConfig properties;
    protected final ScheduledThreadPoolExecutor scheduler;
    protected final String name;
    protected final AbstractEventLoop[] eventLoops;
    private volatile int state = STATE_NOT_INIT;

    public AbstractEventLoopManager(EventLoopManagerConfig properties) {
        this.properties = properties;
        this.name = properties.getName();
        // TODO check 2^n
        int size = properties.getSize();
        if (size <= 0) {
            size = Runtime.getRuntime().availableProcessors();
        }
        this.size = size;
        this.mask = size - 1;
        eventLoops = new AbstractEventLoop[size];
        ThreadFactory globalSchedulerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat(name + "-GlobalScheduler-%d")
            .build();
        int globalSchedulerSize = properties.getGlobalSchedulerSize();
        if (globalSchedulerSize <= 0) {
            globalSchedulerSize = Runtime.getRuntime().availableProcessors() * 2;
        }
        scheduler = new ScheduledThreadPoolExecutor(globalSchedulerSize, globalSchedulerThreadFactory);
    }

    /**
     * 由子类发起调用 初始化 eventLoops
     */
    protected void initEventLoop() {
        for (int i = 0; i < size; i++) {
            String name = this.name + "-" + i;
            SingleThreadFactory tf = new SingleThreadFactory(name + "-EventLoop");
            EventLoopConfig config = new EventLoopConfig();
            config.setName(name);
            config.setIndex(i);
            config.setManagerConfig(this.properties);
            config.setEventLoopThreadFactory(tf);
            eventLoops[i] = createEventLoop(config);
        }
    }

    protected abstract AbstractEventLoop createEventLoop(EventLoopConfig config);

    @Override
    public final <P> void publish1(int hash, P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        int index = index(hash);
        eventLoops[index].publish0(null, payload, arg1, arg2, consumer);
    }

    @Override
    public final <P> void publish2(int hash, Object type, P payload, Object arg1, Object arg2) {
        int index = index(hash);
        eventLoops[index].publish2(type, payload, arg1, arg2);
    }

    @Override
    public AbstractEventLoop eventLoop(int index) {
        return eventLoops[index];
    }

    @Override
    public EventLoop eventLoopByHash(int hash) {
        return eventLoop(hash & mask);
    }

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

    protected void doStart() {
        for (int i = 0; i < size; i++) {
            eventLoops[i].start();
        }
    }

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

    protected void doStop() {
        for (int i = 0; i < size; i++) {
            eventLoops[i].stop();
        }
    }

    @Override
    public final ScheduledExecutorService globalScheduler() {
        return scheduler;
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    public final <P> void register2(Object type, ConsumerFactory<P> factory) {
        for (int i = 0; i < size; i++) {
            eventLoops[i].register2(type, factory);
        }
    }

    @Override
    public final <P> void register3(int type, ConsumerFactory<P> factory) {
        for (int i = 0; i < size; i++) {
            eventLoops[i].register3(type, factory);
        }
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
        BatchPublish[] subBatches = new BatchPublish[size];
        for (BatchPublish.Event<?> event : events) {
            int index = index(event.hash);
            BatchPublish subBatch = subBatches[index];
            if (subBatch == null) {
                subBatches[index] = subBatch = new BatchPublish();
            }
            subBatch.getEvents().add(event);
        }
        for (int i = 0; i < subBatches.length; i++) {
            BatchPublish subBatch = subBatches[i];
            if (subBatch != null) {
                eventLoops[i].batchPublish(subBatch);
            }
        }
    }

    @Override
    public <P> void broadcast1(P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        for (int i = 0; i < size; i++) {
            eventLoops[i].publish1(payload, arg1, arg2, consumer);
        }
    }

    @Override
    public <P> void broadcast2(Object type, P payload, Object arg1, Object arg2) {
        for (int i = 0; i < size; i++) {
            eventLoops[i].publish2(type, payload, arg1, arg2);
        }
    }

    @Override
    public <P> void publish3(int hash, int type, P payload, Object arg1, Object arg2) {
        int index = index(hash);
        eventLoops[index].publish3(type, payload, arg1, arg2);
    }

    @Override
    public <P> void broadcast3(int type, P payload, Object arg1, Object arg2) {
        for (int i = 0; i < size; i++) {
            eventLoops[i].publish3(type, payload, arg1, arg2);
        }
    }

    private int index(int hash) {
        return hash & mask;
    }
}
