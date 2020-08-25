package com.codelifeliwan.rpc.server;

import com.codelifeliwan.rpc.server.config.BeanScope;
import com.codelifeliwan.rpc.server.config.Configuration;
import com.codelifeliwan.rpc.server.config.ThreadType;
import com.codelifeliwan.rpc.server.starter.RPCServerStarter;
import org.yaml.snakeyaml.Yaml;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Map;

/**
 * @author LiWan
 * <p>
 * RPC服务器类
 * 实例化不同的RPCServer可以创建多个server
 */
public class RPCServer implements Closeable {
    private RPCServerStarter serverStarter;

    public RPCServer() throws Exception {
        serverStarter = new RPCServerStarter();
    }

    public RPCServer(Configuration configuration) throws Exception {
        serverStarter = new RPCServerStarter(configuration);
    }

    public void start() throws Exception {
        serverStarter.start();
    }

    public void close() {
        serverStarter.close();
    }

    public Configuration getConfiguration() {
        return serverStarter.getConfiguration();
    }

    /**
     * 从yml文件解析一个server
     *
     * @param inputStream 输入文件
     * @return
     * @throws Exception
     */
    public static RPCServer fromYaml(InputStream inputStream) throws Exception {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);

        if (!map.containsKey("server"))
            throw new Exception("server config not found");
        map = (Map<String, Object>) map.get("server");

        Configuration configuration = Configuration.getInstance();

        if (!map.containsKey("port"))
            throw new Exception("server.port config not found");

        configuration.setListeningPort(Integer.valueOf("" + map.get("port")));

        if (map.containsKey("serializer") && map.get("serializer") != null)
            configuration.setMessageSerializer((String) map.get("serializer"));

        if (map.containsKey("thread") && map.get("thread") != null) {
            String threadType = (String) map.get("thread");
            ThreadType t = ThreadType.getThreadType(threadType);
            if (t == null) throw new Exception("server.thread config error");
            configuration.setThreadType(t);
        }

        if (map.containsKey("beans")) {
            Map<String, Object> beansMap = (Map<String, Object>) map.get("beans");
            for (String key : beansMap.keySet()) {
                String value = (String) beansMap.get(key);
                String[] vs = value.split(",");
                if (vs.length == 1) {
                    configuration.addBean(key, Class.forName(value.trim()), BeanScope.SINGLETON);
                } else {
                    String className = vs[0].trim();
                    String scope = vs[1].trim();
                    if (scope.equalsIgnoreCase("singleton")) {
                        configuration.addBean(key, Class.forName(value.trim()), BeanScope.SINGLETON);
                    } else if (scope.equalsIgnoreCase("prototype")) {
                        configuration.addBean(key, Class.forName(value.trim()), BeanScope.PROTOTYPE);
                    } else {
                        throw new Exception("unknown bean mode:" + scope);
                    }
                }
            }
        }

        RPCServer server = new RPCServer(configuration);
        return server;
    }
}
