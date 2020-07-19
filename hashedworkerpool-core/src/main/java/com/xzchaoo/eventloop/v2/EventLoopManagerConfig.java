package com.xzchaoo.eventloop.v2;

import lombok.Data;

/**
 * created at 2020/7/19
 *
 * @author xzchaoo
 */
@Data
public class EventLoopManagerConfig {
    /**
     * Required
     */
    private String name;
    private int bufferSize = 65536;
    private int size = Runtime.getRuntime().availableProcessors();
    private boolean blockOnInsufficientCapacity = false;
}
