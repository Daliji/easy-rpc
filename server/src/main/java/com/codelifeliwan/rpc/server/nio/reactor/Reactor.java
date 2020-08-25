package com.codelifeliwan.rpc.server.nio.reactor;

import com.codelifeliwan.rpc.server.config.Configuration;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.*;

/**
 * @author LiWan
 * <p>
 * 主从Reactor多线程 模式处理，本类是主Reactor，只有一个线程
 * 主Reactor线程只负责连接的建立工作，具体的服务器通讯和IO操作放在Acceptor里面实现
 * <p>
 * 默认的Acceptor工作线程数（即Acceptor个数）为CPU线程数*2，可自主设置
 */
public class Reactor extends Thread {
    private static final Logger log = Logger.getLogger(Reactor.class);

    private volatile boolean started = false;

    /**
     * 业务处理的线程池
     * 默认为自动伸缩的线程池
     */
    private Executor processThreadPool;

    /**
     * 执行Accestor的线程池
     */
    private Executor accestorThreadPool;

    /**
     * 服务器配置信息
     */
    private Configuration configuration;

    private Acceptor[] acceptors;
    private volatile int acceptorCount = 1;

    /**
     * 服务器事件监听，只监听 ACCEPT 事件
     */
    private ServerSocketChannel channel;

    public Reactor(Configuration configuration, int acceptorCount) throws Exception {
        this(null, configuration, acceptorCount);
    }

    public Reactor(Configuration configuration) throws Exception {
        this(null, configuration);
    }

    public Reactor(Executor processThreadPool, Configuration configuration) throws Exception {
        this(processThreadPool, configuration, Runtime.getRuntime().availableProcessors() * 10);
    }

    public Reactor(Executor processThreadPool, Configuration configuration, int acceptorCount) throws Exception {
        if (processThreadPool != null) {
            this.processThreadPool = processThreadPool;
        } else {
            /*
             * 线程池参数详解：
             * 1、corePoolSize: 核心线程数量，线程池中会存在这么多个线程，当线程数量（包含空闲线程）少于corePoolSize的时候，
             *                  会优先创建新线程，可以设置allowCoreThreadTimeOut=true来让核心线程池中线程也移除
             *
             * 2、maximumPoolSize: 线程池的最大容量，线程池中的线程数量不得超过这么多个，除非阻塞队列设置为无界的
             *
             * 3、keepAliveTime: 空闲线程存活时间，线程空闲超过这个时间的时候就会销毁
             *
             * 4、unit: keepAliveTime的时间单位，分钟、秒等
             *
             * 5、workQueue: 线程工作队列，阻塞队列，线程池从这个队列中取线程，可以设置的队列类型（容量为:capacity）：
             *          ArrayBlockingQueue：有界阻塞队列，当线程数量n：corePoolSize <= n < maximumPoolSize 且 n >= capacity :创建新线程处理任务
             *                                          当：n >= maximumPoolSize 且 n >= capacity 拒绝线程
             *          LinkedBlockingQueue: 无界队列，maximumPoolSize不起作用，会一直创建线程
             *          SynchronousQuene: 不缓存任务，直接调度执行，线程数超过 maximumPoolSize 则直接拒绝线程
             *          PriorityBlockingQueue: 带优先级的线程队列
             *
             *  6、handler: 拒绝策略，线程数量达到maximumPoolSize时的策略，默认提供了4种：
             *          AbortPolicy: 直接丢弃并抛出异常
             *          CallerRunsPolicy: 线程池没有关闭则直接调用线程的run方法
             *          DiscardPolicy: 直接丢弃任务
             *          DiscardOldestPolicy: 丢弃最早的任务，并尝试把当前任务加入队列
             *
             *  7、threadFactory: 创建线程时使用的工厂，可以对线程进行统一设置，如是否守护线程、线程名等
             */
            this.processThreadPool = new ThreadPoolExecutor(100,
                    100,
                    1,
                    TimeUnit.MINUTES,
                    new LinkedBlockingDeque<>());
        }
        this.configuration = configuration;
        this.acceptorCount = acceptorCount;
    }

    /**
     * 初始化Reactor
     * 初始化IO线程池（Acceptor线程池）并开始执行
     *
     * @throws Exception
     */
    private synchronized void init() throws Exception {
        channel = ServerSocketChannel.open();
        // channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(configuration.getListeningPort()));
        channel.socket().setSoTimeout(20000);

        acceptors = new Acceptor[this.acceptorCount];
        for (int i = 0; i < acceptors.length; i++) {
            acceptors[i] = new Acceptor(processThreadPool, configuration);
        }

        // 初始化并执行IO线程池
        accestorThreadPool = new ThreadPoolExecutor(acceptors.length,
                acceptors.length,
                1,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>());

        for (Acceptor acceptor : acceptors) {
            accestorThreadPool.execute(acceptor);
        }
    }


    /**
     * 复写run方法
     */
    public synchronized void run() {
        if (started) {
            log.info("Thread " + Thread.currentThread().getId() + " already started.");
            return;
        }

        started = true;

        try {
            init();

            // 轮流使用Acceptor执行IO任务
            int round = 0; // 下一个要使用的Acceptor指针
            acceptorCount = acceptors.length;
            while (started && (!Thread.currentThread().isInterrupted())) {

                // 此处会阻塞到有连接请求为止
                SocketChannel c = channel.accept();

                // 将该请求转发到对应的线程上执行IO操作
                c.configureBlocking(false);
                acceptors[round++].registerConnectChannel(c);

                round = round % acceptorCount;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public void shutdown() throws Exception {
        for (Acceptor acceptor : acceptors) acceptor.close();
        started = false;
        channel.close();
        Thread.currentThread().interrupt();
    }
}
