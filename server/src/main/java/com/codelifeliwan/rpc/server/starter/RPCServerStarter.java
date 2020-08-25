package com.codelifeliwan.rpc.server.starter;

import com.codelifeliwan.rpc.server.config.ClassInfo;
import com.codelifeliwan.rpc.server.config.Configuration;
import com.codelifeliwan.rpc.server.config.ThreadType;
import com.codelifeliwan.rpc.server.nio.EventLoop;
import com.codelifeliwan.rpc.server.nio.MultiDataAcceptEventLoop;
import com.codelifeliwan.rpc.server.nio.SingleDataAcceptEventLoop;
import lombok.Getter;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LiWan
 * <p>
 * RPC Server 具体实现
 */
public class RPCServerStarter implements Closeable {
    private static final Logger log = Logger.getLogger(RPCServerStarter.class);

    @Getter
    private Configuration configuration;
    private final Map<String, ClassInfo<?>> beans = new HashMap<>();

    // 线程执行器，根据不通过的线程策略选择
    private EventLoop eventLoop;

    public RPCServerStarter() throws Exception {
        this(Configuration.getInstance());
    }

    public RPCServerStarter(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    private void loadBeans() {
        beans.putAll(configuration.getBeanClasses());
    }

    /**
     * 服务器从这里启动
     *
     * @throws Exception
     */
    public void start() throws Exception {
        loadBeans();
        log.info("starting server...");


        if (configuration.getThreadType() == ThreadType.SINGLE) {
            eventLoop = new SingleDataAcceptEventLoop(configuration);
            eventLoop.startLoop();
        } else if (configuration.getThreadType() == ThreadType.MULTI) {
            eventLoop = new MultiDataAcceptEventLoop(configuration);
            eventLoop.startLoop();
        } else {
            throw new Exception("incorrect thread type.");
        }

        log.info("server started at port:" + configuration.getListeningPort());
    }

    @Override
    public void close() {
        try {
            eventLoop.stop();
            log.info("server closed.");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}
