package com.xzchaoo.hashedworkerpool.ext;

import lombok.Data;

/**
 * created at 2020/3/19
 *
 * @author xzchaoo
 */
@Data
public class ManagerProperties {
    private String name = "default";
    private int size = 1;
    private int ringBufferSize = 65536;
    /**
     * default to CPU * 2
     */
    private int globalSchedulerSize;
}
