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

    private static final int                         STATE_NOT_INIT = 0;
    private static final int                         STATE_RUNNING  = 1;
    private static final int                         STATE_STOPPED  = 2;
    protected final      int                         size;
    protected final      int                         mask;
    protected final      EventLoopManagerConfig      properties;
    protected final      ScheduledThreadPoolExecutor scheduler;
    protected final      String                      name;
    protected final      AbstractEventLoop[]         eventLoops;
    private volatile     int                         state          = STATE_NOT_INIT;

    public AbstractEventLoopManager(EventLoopManagerConfig properties) {
        this.properties = properties;
        this.name = properties.getName();
        // TODO check 2^n
        this.size = properties.getSize();
        this.mask = size - 1;
        eventLoops = new AbstractEventLoop[size];
        ThreadFactory globalSchedulerThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat(name + "-GlobalScheduler-%d")
                .build();
        scheduler = new ScheduledThreadPoolExecutor(properties.getGlobalSchedulerSize(), globalSchedulerThreadFactory);
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
    public final <P> void publish1(int hash, P payload, Consumer<P> consumer) {
        publish1(hash, payload, null, null, consumer);
    }

    @Override
    public <P> void publish1(int hash, P payload, Object arg1, Consumer<P> consumer) {
        publish1(hash, payload, arg1, null, consumer);
    }

    @Override
    public final <P> void publish1(int hash, P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        int index = index(hash);
        eventLoop(index).publish0(null, payload, arg1, arg2, consumer);
    }

    @Override
    public final <P> void publish2(int hash, Object type, P payload) {
        publish2(hash, 0, payload, null, null);
    }

    @Override
    public <P> void publish2(int hash, Object type, P payload, Object arg1) {
        publish2(hash, type, payload, arg1, null);
    }

    @Override
    public final <P> void publish2(int hash, Object type, P payload, Object arg1, Object arg2) {
        int index = index(hash);
        eventLoop(index).publish2(type, payload, arg1, arg2);
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
            eventLoop(i).start();
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
            eventLoop(i).stop();
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

    /**
     * @param type
     * @param consumer 必须是线程安全的实现
     * @param <P>
     */
    public final <P> void register(Object type, Consumer<P> consumer) {
        for (int i = 0; i < size; i++) {
            eventLoop(i).register(type, consumer);
        }
    }

    public final <P> void register(Object type, ConsumerFactory<P> factory) {
        for (int i = 0; i < size; i++) {
            eventLoop(i).register(type, factory);
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
                eventLoop(i).batchPublish(subBatch);
            }
        }
    }

    private int index(int hash) {
        return hash & mask;
    }
}
