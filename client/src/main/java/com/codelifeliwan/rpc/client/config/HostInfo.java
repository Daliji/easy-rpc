package com.codelifeliwan.rpc.client.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HostInfo {
    private String host;
    private int port;

    public HostInfo() {
    }

    public HostInfo(String host, int port) {
        this(host + ":" + port);
    }

    public HostInfo(String hostAndPort) {
        String[] hp = hostAndPort.split(":");
        this.host = hp[0].trim();
        this.port = Integer.parseInt(hp[1].trim());
    }
}
