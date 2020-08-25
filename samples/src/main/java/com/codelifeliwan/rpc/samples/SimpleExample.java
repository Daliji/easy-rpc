package com.codelifeliwan.rpc.samples;

import com.codelifeliwan.rpc.client.RPCClient;
import com.codelifeliwan.rpc.samples.models.client.SampleDoClientInterface;
import com.codelifeliwan.rpc.samples.models.client.SampleSort;
import com.codelifeliwan.rpc.samples.models.server.SampleDoServerClass;
import com.codelifeliwan.rpc.samples.models.server.SampleDoSort;
import com.codelifeliwan.rpc.server.RPCServer;
import com.codelifeliwan.rpc.server.config.ThreadType;

import java.util.Random;

/**
 * @author LiWan
 * <p>
 * 简单的使用示例
 */
public class SimpleExample {
    public static void main(String[] args) throws Exception {
        // simpleTest();
        singleSortTest();
    }

    /**
     * 单线程服务器排序性能测试
     * 对应的多线程服务器排序性能测试请看 MultiSortExampleFromYml
     * 本方法测试后耗时约 1.725 秒
     *
     * @throws Exception
     */
    public static void singleSortTest() throws Exception {
        // 创建并启动server
        RPCServer server = new RPCServer();
        server.getConfiguration().setListeningPort(8912);
        server.getConfiguration().addBean("sortBean", SampleDoSort.class); // 添加可被远程调用的对象
        server.start();

        RPCClient client = new RPCClient("192.168.50.45", 8912);
        client.getConfiguration().addBean(SampleSort.class, "sortBean"); // 为client注册接口

        SampleSort bean = client.getBean(SampleSort.class);

        Runnable r = () -> {
            int size = 10000;
            int[] nums = new int[size];
            Random random = new Random(System.currentTimeMillis());
            for (int i = 0; i < size; i++) nums[i] = random.nextInt(1000);
            try {
                nums = bean.sort(nums);
            } catch (Exception e) {
            }
        };

        int threadCount = 500;
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) threads[i] = new Thread(r);

        long startTime = System.currentTimeMillis();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        long endTime = System.currentTimeMillis();

        System.out.println("time used:" + (endTime - startTime) / 1000.0 + " s");
    }

    /**
     * 简单加法测试
     *
     * @throws Exception
     */
    public static void simpleTest() throws Exception {
        // 创建并启动server
        RPCServer server = new RPCServer();
        server.getConfiguration().setListeningPort(8900);
        server.getConfiguration().addBean("addBean", SampleDoServerClass.class); // 添加可被远程调用的对象
        server.start();

        RPCClient client = new RPCClient("localhost", 8912);
        client.getConfiguration().addBean(SampleDoClientInterface.class, "addBean"); // 为client注册接口

        SampleDoClientInterface bean = client.getBean(SampleDoClientInterface.class);

        int c = bean.add(1, 2);
        System.out.println("1 + 2 = " + c);

        // server.close();
    }

    /**
     * 启动服务器示例
     *
     * @throws Exception
     */
    public static void startServer() throws Exception {
        RPCServer server = new RPCServer();

        // 设置server监听的服务器端口
        server.getConfiguration().setListeningPort(8912);

        // 设置server上可以被执行的bean和对应的类
        server.getConfiguration().addBean("addBean", SampleDoServerClass.class);

        // 默认服务器使用单线程运行，执行具有原子性
        // 如果你想以IO多路复用机制运行（多线程，效率高），则执行下面这句
        server.getConfiguration().setThreadType(ThreadType.MULTI);

        // 启动服务器
        server.start();
    }

    public static RPCClient getClient() throws Exception {
        // 实例化客户端并指定调用的服务器地址和端口（可以有多个，会自动负载均衡）
        RPCClient client = new RPCClient("localhost", 8912);

        // 通过接口注册客户端上能执行的方法
        client.getConfiguration().addBean(SampleDoClientInterface.class, "addBean");

        return client;
    }
}
