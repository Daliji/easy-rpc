package com.codelifeliwan.rpc.samples.models.client;

/**
 * 定义客户端的实现调用接口，与Nybatis类似
 * 如果需要在本地调用服务器端的方法，直接定义接口即可
 */
public interface SampleDoClientInterface {
    /**
     * 这里只定义一个简单的int的加法
     *
     * @param a
     * @param b
     * @return
     */
    int add(int a, int b);
}
