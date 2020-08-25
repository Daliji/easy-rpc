package com.codelifeliwan.rpc.server.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassInfo<T> {
    private Class<T> clazz;
    private BeanScope scope;

    private T defaultObject;

    public ClassInfo(Class<T> clazz, BeanScope scope) throws Exception {
        this.clazz = clazz;
        this.scope = scope;

        // 单例模式自动创建对象
        if (scope == BeanScope.SINGLETON) defaultObject = clazz.getConstructor().newInstance();
    }
}
