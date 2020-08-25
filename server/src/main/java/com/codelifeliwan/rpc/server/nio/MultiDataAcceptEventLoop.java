package com.codelifeliwan.rpc.server.nio;

import com.codelifeliwan.rpc.server.config.Configuration;
import com.codelifeliwan.rpc.server.nio.reactor.Reactor;
import org.apache.log4j.Logger;

import java.util.concurrent.Executor;

/**
 * @author LiWan
 * <p>
 * 多线程接收和处理数据
 */
public class MultiDataAcceptEventLoop extends AbstractDataAccessEventLoop {
    private static final Logger log = Logger.getLogger(MultiDataAcceptEventLoop.class);

    private Reactor reactor;

    /**
     * @param configuration 服务器配置
     * @throws Exception
     */
    public MultiDataAcceptEventLoop(Configuration configuration) throws Exception {
        super(configuration);
        this.reactor = new Reactor(configuration);
    }

    @Override
    public Reactor getReactor() {
        return reactor;
    }

    /**
     * @param configuration     服务器配置
     * @param processThreadPool 自定义的线程执行器
     * @throws Exception
     */
    public MultiDataAcceptEventLoop(Configuration configuration, Executor processThreadPool) throws Exception {
        super(configuration);
        this.reactor = new Reactor(processThreadPool, configuration);
    }

    /**
     * @param configuration 服务器配置
     * @param acceptorCount 读写IO线程数量
     * @throws Exception
     */
    public MultiDataAcceptEventLoop(Configuration configuration, int acceptorCount) throws Exception {
        super(configuration);
        this.reactor = new Reactor(configuration, acceptorCount);
    }

    /**
     * @param configuration     服务器配置
     * @param processThreadPool 自定义线程执行器
     * @param acceptorCount     读写IO线程数量
     * @throws Exception
     */
    public MultiDataAcceptEventLoop(Configuration configuration, Executor processThreadPool, int acceptorCount) throws Exception {
        super(configuration);
        this.reactor = new Reactor(processThreadPool, configuration, acceptorCount);
    }
}
