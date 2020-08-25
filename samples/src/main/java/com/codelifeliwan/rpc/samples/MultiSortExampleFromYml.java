package com.codelifeliwan.rpc.samples;

import com.codelifeliwan.rpc.client.RPCClient;
import com.codelifeliwan.rpc.samples.models.client.SampleSort;
import com.codelifeliwan.rpc.server.RPCServer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LiWan
 * <p>
 * 多线程排序测试
 */
public class MultiSortExampleFromYml {

    public static void main(String[] args) throws Exception {
        testMultiSort();
    }

    /**
     * 测试并发排序
     *
     * @throws Exception
     */
    public static void testMultiSort() throws Exception {
        startServer();
        RPCClient client = getClient();

        SampleSort sortBean = client.getBean(SampleSort.class);

        // 测试多线程排序
        Runnable r = () -> {
            int size = 1000;
            int[] nums = new int[size];
            Random random = new Random(System.currentTimeMillis());
            for (int i = 0; i < size; i++) nums[i] = random.nextInt(1000);
            try {
                nums = sortBean.sort(nums);
                System.out.println(nums.length);
            } catch (Exception e) {
            }
        };

        int threadCount = 50;
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) threads[i] = new Thread(r);

        long startTime = System.currentTimeMillis();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        long endTime = System.currentTimeMillis();

        System.out.println("time used:" + (endTime - startTime) / 1000.0 + " s");
    }

    /**
     * 获取一个client
     *
     * @throws Exception
     */
    public static RPCClient getClient() throws Exception {
        InputStream inputStream = MultiSortExampleFromYml.class.getClassLoader().getResourceAsStream("easyrpc.yml");
        return RPCClient.fromYaml(inputStream);
    }

    /**
     * 启动 server
     *
     * @throws Exception
     */
    public static void startServer() throws Exception {
        InputStream inputStream = MultiSortExampleFromYml.class.getClassLoader().getResourceAsStream("easyrpc.yml");
        RPCServer server = RPCServer.fromYaml(inputStream);
        server.start();
    }
}
