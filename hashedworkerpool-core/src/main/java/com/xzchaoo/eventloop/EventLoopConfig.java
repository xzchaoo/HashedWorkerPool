package com.xzchaoo.eventloop;

import lombok.Data;

/**
 * @author xiangfeng.xzc
 * @date 2020-03-20
 */
@Data
public class EventLoopConfig {
    private String                 name;
    private int                    index;
    private EventLoopManagerConfig managerConfig;
    private SingleThreadFactory    eventLoopThreadFactory;
}
