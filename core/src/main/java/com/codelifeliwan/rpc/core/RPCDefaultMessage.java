package com.codelifeliwan.rpc.core;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 发送的Message
 */
public class RPCDefaultMessage implements Serializable {
    private static final long serialVersionUID = 23875263L;

    /**
     * RPC 的bean-name
     */
    @Getter
    @Setter
    private String beanName;

    /**
     * 发送的消息体，包括方法名和参数值
     */
    @Getter
    @Setter
    private String methodName;

    @Getter
    @Setter
    private int status;

    /**
     * 发送参数，设置成String，方便序列化和反序列化
     */
    @Getter
    @Setter
    private String paramsString;

    /**
     * 解析后的参数
     */
    @Setter
    @Getter
    private transient Object[] params;

    /**
     * 接收的消息结果值
     */
    @Getter
    @Setter
    private String valueString;

    /**
     * 解析后的返回值
     */
    @Setter
    @Getter
    private transient Object value;

    public void setParamValuesToString(Object[] objs) {
        if (objs == null) {
            paramsString = null;
            return;
        }
        paramsString = JSON.toJSONString(objs);
    }

    public void setValueToString(Object value) {
        if (value == null) {
            this.valueString = null;
            return;
        }
        this.valueString = JSON.toJSONString(value);
    }
}
