package com.codelifeliwan.rpc.server.nio.reactor;

import com.codelifeliwan.rpc.core.RPCDefaultMessage;
import com.codelifeliwan.rpc.core.RPCStatus;
import com.codelifeliwan.rpc.core.serializer.MessageSerializer;
import com.codelifeliwan.rpc.core.tcp.TCPDataSeparator;
import com.codelifeliwan.rpc.server.config.BeanScope;
import com.codelifeliwan.rpc.server.config.ClassInfo;
import com.codelifeliwan.rpc.server.config.Configuration;
import com.codelifeliwan.rpc.core.RPCByteBuffer;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LiWan
 * <p>
 * 负责具体的消息序列化、方法调用等操作
 */
public class SocketHandler implements Runnable {
    private static final Logger log = Logger.getLogger(SocketHandler.class);

    private Configuration configuration;
    private Map<String, ClassInfo<?>> beans;
    private RPCByteBuffer buffer;

    /**
     * Method的缓存
     * key = beanName-methodName
     */
    private static Map<String, Method> methodsCache = new HashMap<>();

    /**
     * 消息会从该socket解析,channel.socket()
     * 并通过该socket写回消息，用完后关闭
     */
    private SocketChannel channel;

    public SocketHandler(Configuration configuration, SocketChannel channel, RPCByteBuffer buffer) {
        this.configuration = configuration;
        this.channel = channel;
        this.beans = configuration.getBeanClasses();
        this.buffer = buffer;

        if (methodsCache.size() == 0) initMethodCache(configuration);
    }

    private synchronized static void initMethodCache(Configuration configuration) {
        if (configuration == null || methodsCache.size() > 0) return;

        Map<String, ClassInfo<?>> classes = configuration.getBeanClasses();
        for (String beanName : classes.keySet()) {
            Class<?> clazz = classes.get(beanName).getClazz();
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                String key = beanName + "-" + method.getName();
                if (methodsCache.containsKey(key)) {
                    log.error("bean existed : " + key + ", one bean should have no same method name.");
                } else {
                    methodsCache.put(key, method);
                }
            }
        }
    }

    @Override
    public void run() {
        MessageSerializer serializer = configuration.getMessageSerializer();

        try {
            byte[] bytes = this.buffer.getByteArray();
            String requestStr = new String(bytes);
            RPCDefaultMessage fromMessage = (RPCDefaultMessage) serializer.unSerializeMessage(requestStr, null, null);

            if (!beans.containsKey(fromMessage.getBeanName())) {
                String warn = "unknown message : " + fromMessage.getBeanName();
                log.error(warn);
                throw new Exception(warn);
            }

            ClassInfo bean = beans.get(fromMessage.getBeanName());

            Object beanObj = null;
            if (bean.getScope() == BeanScope.SINGLETON) {
                // 单例模式
                beanObj = bean.getDefaultObject();
            } else {
                // 原型模式
                beanObj = bean.getClazz().getConstructor().newInstance();
            }

            // 实际方法调用处理过程, 这次重新序列化类型
            String key = fromMessage.getBeanName() + "-" + fromMessage.getMethodName();
            RPCDefaultMessage response = processMessage(beanObj, methodsCache.get(key), requestStr);

            // 将返回结果写回
            writeToChannel(response);
        } catch (Exception e) {
            e.printStackTrace();
            RPCDefaultMessage response = new RPCDefaultMessage();
            response.setStatus(RPCStatus.GENERAL_ERROR);
            response.setValue("error:" + e.getMessage());
            try {
                writeToChannel(response);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                channel.close();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }

    /**
     * 向channel写数据
     *
     * @param response
     * @throws Exception
     */
    private void writeToChannel(RPCDefaultMessage response) throws Exception {
        MessageSerializer serializer = configuration.getMessageSerializer();
        String responseStr = serializer.serializeMessage(response);

        // 由于TCP数据包有长度限制，所以这里循环写入
        ByteBuffer[] bufs = TCPDataSeparator.split(responseStr);

        if (bufs.length == 0) {
            channel.write(ByteBuffer.allocate(0));
        } else {
            for (ByteBuffer bf : bufs) {
                channel.write(bf);
            }
        }
        channel.shutdownOutput();
    }


    /**
     * 具体调用方法实现,利用反射机制来实现方法调用
     *
     * @param bean   实例化的bean
     * @param method 需要调用的method
     * @return
     * @throws Exception
     */
    private RPCDefaultMessage processMessage(Object bean, Method method, String requestStr) throws Exception {
        if (method == null) throw new Exception("no such method error from bean : " + bean.getClass().getName());
        MessageSerializer serializer = configuration.getMessageSerializer();

        RPCDefaultMessage response = (RPCDefaultMessage) serializer.unSerializeMessage(requestStr, method.getParameterTypes(), null);

        response.setValueToString(method.invoke(bean, response.getParams()));
        response.setParamValuesToString(null);
        return response;
    }
}
