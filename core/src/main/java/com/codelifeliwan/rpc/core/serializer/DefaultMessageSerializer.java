package com.codelifeliwan.rpc.core.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.codelifeliwan.rpc.core.RPCDefaultMessage;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 消息序列化和反序列化默认的类
 * 可以自定义自己的序列化和反序列化类
 * <p>
 * 使用Gson进行序列化和反序列化
 */
public class DefaultMessageSerializer implements MessageSerializer<RPCDefaultMessage> {
    private static final Logger log = Logger.getLogger(DefaultMessageSerializer.class);

    @Override
    public RPCDefaultMessage unSerializeMessage(String input, Class<?>[] methodParamTypes, Class<?> returnType) throws Exception {
        if (input == null) return new RPCDefaultMessage();
        RPCDefaultMessage result = JSON.parseObject(input, RPCDefaultMessage.class);

        if (methodParamTypes == null && returnType == null) return result;

        if (methodParamTypes != null && result.getParamsString() != null) {
            if (methodParamTypes.length == 0) {
                result.setParams(new Object[0]);
            } else {
                JSONArray array = JSON.parseArray(result.getParamsString());
                Object[] objs = new Object[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    objs[i] = parse(JSON.toJSONString(array.get(i)), methodParamTypes[i]);
                }
                result.setParams(objs);
            }
        }

        if (returnType != null && result.getValueString() != null) {
            result.setValue(parse(result.getValueString(), returnType));
        }

        return result;
    }

    /**
     * 针对序列化，目前只支持数组和List
     *
     * @param s
     * @param classType
     * @return
     * @throws Exception
     */
    private Object parse(String s, Class<?> classType) throws Exception {
        if (s == null || classType == null) return null;
        Object result = null;

        try {
            result = JSON.parseObject(s, classType);
        } catch (Exception e) {
            try {
                List<?> arr = JSON.parseArray(s, classType);
                if (classType.isArray()) {
                    Object[] os = new Object[arr.size()];
                    for (int i = 0; i < os.length; i++) os[i] = arr.get(i);
                    result = os;
                } else {
                    result = arr;
                }
            } catch (Exception e1) {
                throw e1;
            }
        }

        return result;
    }

    @Override
    public String serializeMessage(RPCDefaultMessage obj) throws Exception {
        if (obj == null) return "{}";
        return JSON.toJSONString(obj);
    }
}
