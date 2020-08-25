package com.codelifeliwan.rpc.client;

import com.codelifeliwan.rpc.client.config.Configuration;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * RPC客户端
 */
public class RPCClient {
    private static final Logger log = Logger.getLogger(RPCClient.class);

    @Getter
    private Configuration configuration;

    public RPCClient(String host, int port) {
        this(new Configuration(host, port));
    }

    public RPCClient(Configuration configuration) {
        this.configuration = configuration;
    }

    public <T> T getBean(Class<T> clazz) {
        return configuration.getBean(clazz);
    }

    /**
     * 从yml文件解析一个client
     *
     * @param inputStream 输入文件
     * @return
     * @throws Exception
     */
    public static RPCClient fromYaml(InputStream inputStream) throws Exception {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);

        if (!map.containsKey("client"))
            throw new Exception("client config not found");
        map = (Map<String, Object>) map.get("client");

        if (!map.containsKey("server"))
            throw new Exception("client.server config not found");

        String serverString = (String) map.get("server");

        if (serverString == null || serverString.trim().length() == 0)
            throw new Exception("client.server config error");

        Configuration configuration = new Configuration(serverString);

        if (map.containsKey("serializer") && map.get("serializer") != null)
            configuration.setMessageSerializer((String) map.get("serializer"));

        if (map.containsKey("beans")) {
            Map<String, Object> beansMap = (Map<String, Object>) map.get("beans");
            for (String key : beansMap.keySet()) configuration.addBean(Class.forName((String) beansMap.get(key)), key);
        }

        RPCClient client = new RPCClient(configuration);
        return client;
    }
}
