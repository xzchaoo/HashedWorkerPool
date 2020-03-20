package com.xzchaoo.eventloop;

import lombok.Data;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
@Data
public class EventLoopManagerConfig {
    private String name                = "default";
    private int    size                = 1;
    private int    eventLoopBufferSize = 65536;
    /**
     * default to CPU * 2
     */
    private int    globalSchedulerSize;
}
