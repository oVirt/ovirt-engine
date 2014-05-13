package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;

public class ForemanComputerResource implements Serializable {
    private static final long serialVersionUID = -3185315137494277207L;
    private String name;
    private String url;
    private int id;

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
