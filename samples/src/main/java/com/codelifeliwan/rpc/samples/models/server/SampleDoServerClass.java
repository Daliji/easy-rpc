package com.codelifeliwan.rpc.samples.models.server;

/**
 * server实现层
 * 在server端定义一个具体的实现
 */
public class SampleDoServerClass {

    /**
     * 定义一个add方法供客户端调用
     *
     * @param a
     * @param b
     * @return
     */
    public int add(int a, int b) {
        return a + b;
    }
}
