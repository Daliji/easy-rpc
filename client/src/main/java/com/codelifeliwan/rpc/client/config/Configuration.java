package com.codelifeliwan.rpc.client.config;

import com.codelifeliwan.rpc.client.proxy.BeanProxy;
import com.codelifeliwan.rpc.core.serializer.MessageSerializer;
import org.apache.log4j.Logger;

import java.lang.reflect.Proxy;
import java.util.*;

/**
 * 客户端配置
 */
public final class Configuration {
    private static final Logger log = Logger.getLogger(Configuration.class);

    /**
     * 服务器端配置
     * 客户端可以选择性的调用服务器端，起到负载均衡的作用
     */
    private List<HostInfo> hostInfoList = new ArrayList<>();

    private String messageSerializer = "com.codelifeliwan.rpc.core.serializer.DefaultMessageSerializer"; // 默认的消息序列化类
    private MessageSerializer serializer = null;

    /**
     * 实例化的bean对象
     */
    private Map<Class<?>, ClassInfo<?>> beans = new HashMap<>();

    public Configuration(String host, int port) {
        this.hostInfoList.add(new HostInfo(host, port));
    }

    public Configuration(String host, int port, String messageSerializer) {
        this(host, port);
        this.messageSerializer = messageSerializer;
    }

    /**
     * 接收一个长字符串来解析服务器地址
     *
     * @param hostAndPortStrs   RPC服务器信，使用 host1:port1, host2:port2格式
     * @param messageSerializer
     */
    public Configuration(String hostAndPortStrs, String messageSerializer) {
        this(hostAndPortStrs);
        this.messageSerializer = messageSerializer;
    }

    public Configuration(String hostAndPortStrs) {
        String[] hs = hostAndPortStrs.split(",");
        for (String h : hs) {
            h = h.trim();
            if (h.length() == 0) continue;
            hostInfoList.add(new HostInfo(h));
        }
    }

    public <T> T getBean(Class<T> clazz) {
        if (clazz == null || (!beans.containsKey(clazz))) return null;
        ClassInfo<?> info = beans.get(clazz);

        return (T) info.getDefaultObject();
    }

    /**
     * 添加一个bean供调用
     *
     * @param clazz    接口的class信息
     * @param beanName 服务器上的bean-name
     * @throws Exception
     */
    public synchronized void addBean(Class<?> clazz, String beanName) throws Exception {
        if (clazz == null || beanName == null || beans.containsKey(clazz)) throw new Exception("add bean error.");
        ClassInfo info = new ClassInfo<>(clazz);

        /**
         * 生成动态代理对象
         * 使用随机值来选择调用的服务器，起到负载均衡的作用
         */
        Random r = new Random(System.currentTimeMillis());
        int index = r.nextInt(hostInfoList.size());
        HostInfo hostInfo = hostInfoList.get(index);

        info.setDefaultObject(Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{clazz},
                new BeanProxy(hostInfo.getHost(), hostInfo.getPort(), beanName, getMessageSerializer())));

        beans.put(clazz, info);
    }

    public MessageSerializer getMessageSerializer() {
        if (serializer != null) return serializer;
        synchronized (this) {
            if (serializer != null) return serializer;
            try {
                Class<MessageSerializer> c = (Class<MessageSerializer>) Class.forName(messageSerializer);
                serializer = c.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }

        return serializer;
    }

    public void setMessageSerializer(String className) {
        synchronized (this) {
            messageSerializer = className;
            serializer = null;
        }
    }
}
