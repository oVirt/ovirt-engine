package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class ExternalComputeResource implements Serializable {
    private static final long serialVersionUID = -6951116030464852526L;
    private String name;
    private int id;
    private String url;

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
    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
    }
}
