package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

public class GraphicsInfo implements Serializable {

    private static final long serialVersionUID = -633727623243243619L;

    private String ip;
    private Integer port;
    private Integer tlsPort;

    public GraphicsInfo() { }


    public String getIp() {
        return ip;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getTlsPort() {
        return tlsPort;
    }

    public GraphicsInfo setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public GraphicsInfo setPort(Integer port) {
        this.port = port;
        return this;
    }

    public GraphicsInfo setTlsPort(Integer tlsPort) {
        this.tlsPort = tlsPort;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraphicsInfo)) {
            return false;
        }

        GraphicsInfo other = (GraphicsInfo) o;
        return Objects.equals(ip, other.ip)
                && Objects.equals(port, other.port)
                && Objects.equals(tlsPort, other.tlsPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                ip,
                port,
                tlsPort
        );
    }

}
