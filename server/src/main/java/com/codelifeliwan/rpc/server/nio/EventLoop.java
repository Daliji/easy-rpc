package com.codelifeliwan.rpc.server.nio;

public interface EventLoop {

    void startLoop();

    boolean stop();
}
