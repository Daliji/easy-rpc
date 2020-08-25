# easy-rpc

一个及其轻量级的RPC服务，适合用在小型项目上。让你调用远程服务器上的方法像是用本地方法一样简单~

博客及后续文档更新地址：[大栗几的博客](https://juejin.im/user/1222312662953736)

如有问题请发送到：
> @MailTo:codelifeliwan@sina.com
 
与我沟通哦~

## 使用方法
下载完成后直接运行samples下的运行示例即可查看运行效果。
### 1.如何创建一个服务器端
创建服务器端只需要以下简单的几行代码：
```java
public static void startServer() throws Exception {
    RPCServer server = new RPCServer();

    // 设置server监听的服务器端口
    server.getConfiguration().setListeningPort(8900);

    // 设置server上可以被执行的bean和对应的类
    server.getConfiguration().addBean("addBean", SampleDoServerClass.class);

    // 默认服务器使用单线程运行，执行具有原子性
    // 如果你想以IO多路复用机制运行（多线程，效率高），则执行下面这句
    server.getConfiguration().setThreadType(ThreadType.MULTI);

    // 启动服务器
    server.start();
}
```
启动完成以后，客户端即可向服务器发送请求。不同的端口上可以实例化不同的server。

### 2.如何创建一个客户端
创建客户端只需要以下简单的代码：
```java
public static RPCClient getClient() throws Exception {
    // 实例化客户端并指定调用的服务器地址和端口（可以有多个，会自动负载均衡）
    RPCClient client = new RPCClient("localhost", 8900);

    // 通过接口注册客户端上能执行的方法
    client.getConfiguration().addBean(SampleDoClientInterface.class, "addBean");

    return client;
}
```

### 3.如何使用客户端调用远程服务器上的方法
在获取了客户端之后就可以调用了，调用方式：
```java
SampleDoClientInterface bean = client.getBean(SampleDoClientInterface.class);
System.out.println("1 + 2 = " + bean.add(1, 2));
```
只需要简单的获取bean即可使用接口内的方法调用。

#### 4.如何通过配置文件配置客户端和服务器
几行代码就可以完成server的启动和client的调用，参考samples中的方法即可:
```java
package com.codelifeliwan.rpc.samples;

import com.codelifeliwan.rpc.client.RPCClient;
import com.codelifeliwan.rpc.samples.models.cient.*;
import com.codelifeliwan.rpc.server.RPCServer;

import java.io.InputStream;

public class Start {

    public static void main(String[] args) throws Exception {
        startServer();

        RPCClient client = getClient();

        // 调用远程方法
        SampleDoClientInterface bean = client.getBean(SampleDoClientInterface.class);

        System.out.println("10 + 20 = " + bean.add(10, 20));
    }


    /**
     * 获取一个client
     *
     * @throws Exception
     */
    public static RPCClient getClient() throws Exception {
        InputStream inputStream = Start.class.getClassLoader().getResourceAsStream("easyrpc.yml");
        return RPCClient.fromYaml(inputStream);
    }

    /**
     * 启动 server
     *
     * @throws Exception
     */
    public static void startServer() throws Exception {
        InputStream inputStream = Start.class.getClassLoader().getResourceAsStream("easyrpc.yml");
        RPCServer server = RPCServer.fromYaml(inputStream);
        server.start();
    }
}
```

### 5.配置文件示例
```yaml
# easyrpc server config
server:
  # 监听端口
  port: 8912
  # 监听线程模式，有 single (单线程处理，业务执行具有原子性), multi (NIO主从多线程模型，效率较高，暂未实现)
  thread: multi
  # 消息序列化类
  serializer: com.codelifeliwan.rpc.core.serializer.DefaultMessageSerializer
  # 注册的类，格式：bean-name:bean-class, model(singleton/prototype, default is singleton)
  beans:
    testBean: com.codelifeliwan.rpc.samples.models.server.SampleDoServerClass
    sortBean: com.codelifeliwan.rpc.samples.models.server.SampleDoSort

# easyrpc client config
client:
  # 通讯的服务器，多个用逗号隔开，多个服务器的时候会自动负载均衡（随机值hash）
  server: localhost:8912
  # 消息序列化类
  serializer: com.codelifeliwan.rpc.core.serializer.DefaultMessageSerializer
  # 需要被实例化的接口
  beans:
    testBean: com.codelifeliwan.rpc.samples.models.client.SampleDoClientInterface
    sortBean: com.codelifeliwan.rpc.samples.models.client.SampleSort
```

快使用一下吧~