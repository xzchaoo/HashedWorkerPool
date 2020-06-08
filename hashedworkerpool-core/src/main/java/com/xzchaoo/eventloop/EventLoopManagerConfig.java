package com.xzchaoo.eventloop;

import lombok.Data;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
@Data
public class EventLoopManagerConfig {
    private String name = "default";
    /**
     * default to CPU cores
     */
    private int size;
    private int eventLoopBufferSize = 65536;
    /**
     * default to CPU cores * 2
     */
    private int globalSchedulerSize;
}
