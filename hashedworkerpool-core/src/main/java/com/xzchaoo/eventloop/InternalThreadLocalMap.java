package com.xzchaoo.eventloop;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiangfeng.xzc
 */
public class InternalThreadLocalMap {
    /**
     * TODO 可以将一些显式的字段放在这里加速获取
     */
    Map<Integer, Object> map = new HashMap<>();
}
