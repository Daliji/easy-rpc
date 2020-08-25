package com.codelifeliwan.rpc.samples;

import com.codelifeliwan.rpc.client.RPCClient;
import com.codelifeliwan.rpc.samples.models.client.SampleSort;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RemoteSortExample {
    private static AtomicInteger success = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        testRemoteSort();
    }

    /**
     * 测试，1万个数字排序，100个并发
     * <p>
     * 远程环境下，服务器多线程的效率平均为 1.8 秒
     * 远程环境下，服务器线程的效率平均为 1.6 秒
     * <p>
     * 原因：该接口为CPU密集型接口，单线程要比多线程快
     *
     * @throws Exception
     */
    public static void testRemoteSort() throws Exception {
        SortRunnable r = new SortRunnable(getClient());

        int threadCount = 100;
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(r);
        }

        long startTime = System.currentTimeMillis();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        long endTime = System.currentTimeMillis();

        System.out.println("success:" + success.get());
        System.out.println("time used:" + (endTime - startTime) / 1000.0 + " s");
    }

    private static class SortRunnable implements Runnable {
        private RPCClient client;

        SortRunnable(RPCClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            SampleSort sortBean = client.getBean(SampleSort.class);

            int size = 10000;
            int[] nums = new int[size];
            Random random = new Random(System.currentTimeMillis());
            for (int i = 0; i < size; i++) nums[i] = random.nextInt(1000);
            try {
                nums = sortBean.sort(nums);
                success.incrementAndGet();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    public static RPCClient getClient() throws Exception {
        RPCClient client = new RPCClient("192.168.50.45", 8912);
        //RPCClient client = new RPCClient("localhost", 8912);
        client.getConfiguration().addBean(SampleSort.class, "sortBean"); // 为client注册接口
        return client;
    }
}
