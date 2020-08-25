package com.codelifeliwan.rpc.server.config;

import lombok.Getter;

/**
 * 运行模式
 */
public enum ThreadType {
    SINGLE("single"), // 单线程模式
    MULTI("multi"), // Reactor主从多线程模式
    ;

    @Getter
    private String typeName;

    ThreadType(String typeName) {
        this.typeName = typeName;
    }

    public static ThreadType getThreadType(String type) {
        for (ThreadType t : ThreadType.values()) if (t.typeName.equalsIgnoreCase(type)) return t;
        return null;
    }
}
