package com.codelifeliwan.rpc.server.nio;

import com.codelifeliwan.rpc.server.config.Configuration;
import com.codelifeliwan.rpc.server.nio.reactor.Reactor;
import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * 单线程接收和处理数据
 */
public class SingleDataAcceptEventLoop extends AbstractDataAccessEventLoop {
    private static final Logger log = Logger.getLogger(SingleDataAcceptEventLoop.class);
    /**
     * Reactor 单线程模型
     */
    private Reactor reactor;

    /**
     * 实例化只有一个线程的模型
     *
     * @param configuration
     * @throws Exception
     */
    public SingleDataAcceptEventLoop(Configuration configuration) throws Exception {
        super(configuration);
        this.reactor = new Reactor(Executors.newSingleThreadExecutor(), configuration, 1);
    }

    @Override
    public Reactor getReactor() {
        return reactor;
    }
}
