package com.codelifeliwan.rpc.core.serializer;

/**
 * 定义序列化和反序列化模板
 *
 * @param <U> 待序列化的类型
 */
public interface MessageSerializer<U> {

    /**
     * 反序列化对象
     *
     * @param input            待被序列化的对象
     * @param methodParamTypes 方法传参类型
     * @param returnType       返回值类型
     * @return
     * @throws Exception
     */
    U unSerializeMessage(String input, Class<?>[] methodParamTypes, Class<?> returnType) throws Exception;

    /**
     * 序列化对象
     *
     * @param obj 待序列化的对象
     * @throws Exception
     */
    String serializeMessage(U obj) throws Exception;
}
