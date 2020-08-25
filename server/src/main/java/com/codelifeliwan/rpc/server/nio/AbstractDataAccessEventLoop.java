package com.codelifeliwan.rpc.server.nio;

import com.codelifeliwan.rpc.server.config.Configuration;
import com.codelifeliwan.rpc.server.nio.reactor.Reactor;
import org.apache.log4j.Logger;

public abstract class AbstractDataAccessEventLoop implements EventLoop {
    private static final Logger log = Logger.getLogger(AbstractDataAccessEventLoop.class);

    private Configuration configuration;
    private Reactor reactor;

    public AbstractDataAccessEventLoop(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    @Override
    public void startLoop() {
        Runnable r = () -> {
            try {
                reactor = getReactor();
                reactor.start();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        };

        new Thread(r).start();
    }

    @Override
    public boolean stop() {
        try {
            reactor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public abstract Reactor getReactor();
}
