package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class ExternalOperatingSystem implements Serializable {
    private static final long serialVersionUID = 6643860040539656422L;
    private String name;
    private int id;
    private int mediaId;

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
