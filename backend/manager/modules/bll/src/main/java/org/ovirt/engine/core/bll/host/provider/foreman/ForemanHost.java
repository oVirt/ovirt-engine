package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;

public class ForemanHost implements Serializable {
    private static final long serialVersionUID = 468697212133957493L;
    private String name;
    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
