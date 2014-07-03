package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;

public class ForemanComputerResource implements Serializable {
    private static final long serialVersionUID = -3185315137494277207L;
    private String name;
    private String url;
    private int id;
    private String provider;
    private String user;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
