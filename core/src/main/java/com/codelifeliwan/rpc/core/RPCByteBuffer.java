package com.codelifeliwan.rpc.core;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class RPCByteBuffer {

    @Getter
    private ByteBuffer buffer;

    private RPCByteBuffer() {
    }

    public static RPCByteBuffer fromChannel(SocketChannel channel) throws Exception {
        RPCByteBuffer buffer = new RPCByteBuffer();
        List<byte[]> buffers = new ArrayList<>();

        int totleSize = 0;
        ByteBuffer data = ByteBuffer.allocate(1024 * 2);

        while ((channel.read(data)) != -1) {
            data.flip();

            // 出现TCP延时发送不过来的情况，直接阻塞10豪秒再接收
            if (data.remaining() == 0) {
                LockSupport.parkUntil(10);
                data.clear();
                continue;
            }

            totleSize += data.remaining();
            buffers.add(Arrays.copyOfRange(data.array(), data.position(), data.limit()));
            data.clear();
        }

        buffer.buffer = ByteBuffer.allocate(totleSize);
        for (byte[] buf : buffers) buffer.buffer.put(buf);

        return buffer;
    }

    public byte[] getByteArray() {
        buffer.flip();
        return buffer.array();
    }
}
