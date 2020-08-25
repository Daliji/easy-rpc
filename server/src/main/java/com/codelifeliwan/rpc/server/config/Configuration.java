package com.codelifeliwan.rpc.server.config;

import com.codelifeliwan.rpc.core.serializer.MessageSerializer;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC服务器配置类
 */
public final class Configuration {
    private static final Logger log = Logger.getLogger(Configuration.class);
    private static final Configuration instance = new Configuration();

    private Configuration() {
    }

    public static Configuration getInstance() {
        return instance;
    }

    /**
     * 定义需要配置的字段
     */
    private String messageSerializer = "com.codelifeliwan.rpc.core.serializer.DefaultMessageSerializer"; // 默认的消息序列化类
    private MessageSerializer serializer = null;

    /**
     * 监听端口
     */
    @Getter
    @Setter
    private int listeningPort = 8912;

    @Getter
    @Setter
    private ThreadType threadType = ThreadType.SINGLE;

    /**
     * 需要注册的bean，即具体实现的类，类似Spring的Controller
     */
    private final Map<String, ClassInfo<?>> beanClasses = new HashMap<>();

    public Map<String, ClassInfo<?>> getBeanClasses() {
        return new HashMap<>() {{
            putAll(beanClasses);
        }};
    }

    public void addBean(String beanName, Class<?> clazz) throws Exception {
        addBean(beanName, clazz, null);
    }

    public void addBean(String beanName, Class<?> clazz, BeanScope scope) throws Exception {
        if (beanName == null || clazz == null)
            throw new Exception("error register bean name:" + beanName + " and class:" + beanClasses);
        if (beanClasses.containsKey(beanName))
            throw new Exception("bean name " + beanName + " has already exist.");

        if (scope == null) scope = BeanScope.SINGLETON; // 默认是单例模式

        beanClasses.put(beanName, new ClassInfo<>(clazz, scope));
    }


    public MessageSerializer getMessageSerializer() {
        if (serializer != null) return serializer;
        synchronized (Configuration.class) {
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
        synchronized (Configuration.class) {
            messageSerializer = className;
            serializer = null;
        }
    }
}
