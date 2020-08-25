package com.codelifeliwan.rpc.client.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassInfo<T> {
    private Class<T> clazz;

    private T defaultObject;

    public ClassInfo(Class<T> clazz) throws Exception {
        this.clazz = clazz;
    }
}
