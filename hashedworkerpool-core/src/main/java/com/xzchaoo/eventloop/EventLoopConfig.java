package com.xzchaoo.eventloop;

import lombok.Data;

/**
 * @author xiangfeng.xzc
 */
@Data
public class EventLoopConfig {
    private String                 name;
    private int                    index;
    private EventLoopManagerConfig managerConfig;
    private SingleThreadFactory    eventLoopThreadFactory;
}
