package com.xzchaoo.hashedworkerpool.core;

/**
 * 提供hash值(用于分区)的接口
 *
 * @author xiangfeng.xzc
 * @date 2019/7/22
 */
public interface PartitionKey {
    int getHash();
}
