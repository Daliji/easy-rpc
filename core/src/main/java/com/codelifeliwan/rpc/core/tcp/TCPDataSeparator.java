package com.codelifeliwan.rpc.core.tcp;

import java.nio.ByteBuffer;

/**
 * @author liwan
 * <p>
 * TCP数据手动分割，防止出现大量的TCP拥塞
 */
public class TCPDataSeparator {

    /**
     * UDP 最大报文长度为65535
     * 这里将TCP一次发送的最大长度暂时也设置为 65535
     * TCP自己会自动进行报文分割，这里只是为了以防万一
     */
    private static final int dataLimit = 65535;

    public static ByteBuffer[] split(String s) {
        if (s == null) return new ByteBuffer[0];

        return split(s.getBytes());
    }

    public static ByteBuffer[] split(byte[] bytes) {
        if (bytes == null) return new ByteBuffer[0];

        if (bytes.length < dataLimit) return new ByteBuffer[]{ByteBuffer.wrap(bytes)};

        int line = (bytes.length + dataLimit - 1) / dataLimit;
        ByteBuffer[] result = new ByteBuffer[line];

        for (int i = 0; i < line; i++) {
            int from = dataLimit * i;
            result[i] = ByteBuffer.wrap(bytes, from, i == line - 1 ? bytes.length - from : dataLimit);
        }

        return result;
    }
}
