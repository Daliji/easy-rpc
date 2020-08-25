package com.codelifeliwan.rpc.client.proxy;

import com.codelifeliwan.rpc.core.RPCByteBuffer;
import com.codelifeliwan.rpc.core.RPCDefaultMessage;
import com.codelifeliwan.rpc.core.RPCStatus;
import com.codelifeliwan.rpc.core.serializer.MessageSerializer;
import com.codelifeliwan.rpc.core.tcp.TCPDataSeparator;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 动态代理bean，在bean中调用远程方法
 */
public class BeanProxy implements InvocationHandler {
    private static final Logger log = Logger.getLogger(BeanProxy.class);

    private String host;
    private int port;
    private String beanName;
    private MessageSerializer serializer;

    public BeanProxy(String host, int port, String beanName, MessageSerializer serializer) {
        this.host = host;
        this.port = port;
        this.beanName = beanName;
        this.serializer = serializer;
    }

    /**
     * 在invoke中调用远程方法
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCDefaultMessage message = new RPCDefaultMessage();
        message.setBeanName(beanName);
        message.setMethodName(method.getName());
        message.setParamValuesToString(args);

        RPCDefaultMessage response = call(message, method);

        if (response.getStatus() != RPCStatus.OK)
            throw new Exception("" + response.getValue() + ", error_code=" + response.getStatus());

        return response.getValue();
    }

    /**
     * 调用远程方法
     *
     * @param message
     * @return
     * @throws Exception
     */
    private RPCDefaultMessage call(RPCDefaultMessage message, Method method) throws Exception {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));

        // 向server发送请求
        String requestStr = serializer.serializeMessage(message);

        // 由于TCP数据包有长度限制，所以这里循环写入
        ByteBuffer[] bufs = TCPDataSeparator.split(requestStr);

        if (bufs.length == 0) {
            channel.write(ByteBuffer.allocate(0));
        } else {
            for (ByteBuffer bf : bufs) {
                channel.write(bf);
            }
        }

        channel.shutdownOutput();

        // 获取server的回复
        RPCByteBuffer buffer = RPCByteBuffer.fromChannel(channel);
        byte[] bytes = buffer.getByteArray();
        String responseStr = new String(bytes);
        RPCDefaultMessage responseMessage = (RPCDefaultMessage) serializer.unSerializeMessage(responseStr, null, method.getReturnType());

        channel.shutdownInput();
        channel.close();

        return responseMessage;
    }
}
