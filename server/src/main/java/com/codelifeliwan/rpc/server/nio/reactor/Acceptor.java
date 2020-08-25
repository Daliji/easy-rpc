package com.codelifeliwan.rpc.server.nio.reactor;

import com.codelifeliwan.rpc.server.config.Configuration;
import com.codelifeliwan.rpc.core.RPCByteBuffer;
import lombok.Getter;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author LiWan
 * <p>
 * 负责具体的网络IO操作，并将执行下发到线程池中运行
 */
@Getter
public class Acceptor implements Closeable, Runnable {
    private static final Logger log = Logger.getLogger(Acceptor.class);

    /**
     * 具体处理的线程池，在一个Server内共享该线程池
     */
    private Executor processThreadPool;

    private volatile Selector selector;

    private Configuration configuration;

    private volatile boolean closed = true;

    public Acceptor(Executor processThreadPool, Configuration configuration) throws Exception {
        this.processThreadPool = processThreadPool;
        this.configuration = configuration;
        this.selector = Selector.open();
    }


    @Override
    public void close() throws IOException {
        selector.close();
        closed = true;
        Thread.currentThread().interrupt();
    }

    /**
     * 从主Reactor注册channel，这里只监听OP_CONNECT和OP_READ请求
     *
     * @param sc
     * @throws Exception
     */
    public void registerConnectChannel(SocketChannel sc) throws Exception {
        registerConnectChannel(sc, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
    }

    public void registerConnectChannel(SocketChannel sc, int status) throws Exception {
        sc.register(selector, status, this);
        selector.wakeup();
    }

    /**
     * 读取并处理消息
     */
    public void handleExecutor(SocketChannel channel, RPCByteBuffer buffer) throws Exception {
        processThreadPool.execute(new SocketHandler(configuration, channel, buffer));
    }

    private void handleKey(SelectionKey key) throws Exception {
        if (key.isConnectable()) {
            handleConnectEvent(key);
        } else if (key.isAcceptable()) {
            handleAcceptEvent(key);
        } else if (key.isReadable()) {
            handleReadEvent(key);
        } else if (key.isWritable()) {
            handleOtherEvent(key);
        } else {
            handleOtherEvent(key);
        }
    }

    public void run() {
        closed = false;
        while (!closed && (!Thread.currentThread().isInterrupted())) {
            try {
                int eventCount = selector.select();
                if (eventCount == 0) continue;

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    try {
                        if (!key.isValid()) {
                            key.channel().close();
                            key.cancel();
                            continue;
                        }
                        handleKey(key);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        log.error(e1.getMessage());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }

    /**
     * 处理CONNECT事件
     *
     * @param key
     * @throws Exception
     */
    private void handleConnectEvent(SelectionKey key) throws Exception {
        log.info("*** connectable");
        SocketChannel ch = (SocketChannel) key.channel();
        ch.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }

    /**
     * 读取数据
     *
     * @param key
     * @throws Exception
     */
    private void handleReadEvent(SelectionKey key) throws Exception {
        // log.info("*** readable");
        key.cancel();
        SocketChannel ch = (SocketChannel) key.channel();

        RPCByteBuffer buffer = RPCByteBuffer.fromChannel(ch);
        ch.shutdownInput();
        handleExecutor(ch, buffer);
    }

    /**
     * 测试此键的通道是否已准备好接受新的套接字连接
     * 本项目中，应该是在Reactor中使用，这里不使用
     *
     * @param key
     * @throws Exception
     */
    private void handleAcceptEvent(SelectionKey key) throws Exception {
        log.error("*** acceptable, this is impossiable");
        throw new Exception("no such event : accept");
    }

    /**
     * 写事件等，暂时不使用
     *
     * @param key
     * @throws Exception
     */
    private void handleOtherEvent(SelectionKey key) throws Exception {
        log.error("*** not used");
        throw new Exception("no such event : other");
    }
}
