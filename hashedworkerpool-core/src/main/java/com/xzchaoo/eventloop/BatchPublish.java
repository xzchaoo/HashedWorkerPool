package com.xzchaoo.eventloop;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xiangfeng.xzc
 */
public class BatchPublish {
    @Getter
    @Setter
    private List<Event<?>> events = new ArrayList<>();

    public <P> void publish1(int hash, P payload, Consumer<P> consumer) {
        publish0(hash, null, payload, null, null, consumer);
    }

    public <P> void publish1(int hash, P payload, Object arg1, Consumer<P> consumer) {
        publish0(hash, null, payload, arg1, null, consumer);
    }

    public <P> void publish1(int hash, P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        publish0(hash, null, payload, arg1, arg2, consumer);
    }

    public <P> void publish2(int hash, Object type, P payload) {
        publish0(hash, type, payload, null, null, null);
    }

    public <P> void publish2(int hash, Object type, P payload, Object arg1) {
        publish0(hash, type, payload, arg1, null, null);
    }

    public <P> void publish2(int hash, Object type, P payload, Object arg1, Object arg2) {
        publish0(hash, type, payload, arg1, arg2, null);
    }

    private <P> void publish0(int hash, Object type, P payload, Object arg1, Object arg2, Consumer<P> consumer) {
        Event<P> event = new Event<>();
        event.hash = hash;
        event.type = type;
        event.payload = payload;
        event.arg1 = arg1;
        event.arg2 = arg2;
        event.consumer = consumer;
        events.add(event);
    }

    public static class Event<P> {
        public int         hash;
        public Object      type;
        public P           payload;
        public Object      arg1;
        public Object      arg2;
        public Consumer<P> consumer;
    }
}
