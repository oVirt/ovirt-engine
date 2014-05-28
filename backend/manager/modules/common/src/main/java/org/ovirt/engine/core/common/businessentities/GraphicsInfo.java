package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

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
        if (this == o) return true;
        if (!(o instanceof GraphicsInfo)) return false;

        GraphicsInfo that = (GraphicsInfo) o;

        if (ip != null ? !ip.equals(that.ip) : that.ip != null) return false;
        if (port != null ? !port.equals(that.port) : that.port != null) return false;
        if (tlsPort != null ? !tlsPort.equals(that.tlsPort) : that.tlsPort != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (tlsPort != null ? tlsPort.hashCode() : 0);
        return result;
    }

}
