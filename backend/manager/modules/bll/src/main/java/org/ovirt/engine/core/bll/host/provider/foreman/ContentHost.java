package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;
import java.util.Date;

public class ContentHost implements Serializable {
    private static final long serialVersionUID = -6496042131555889764L;
    private String uuid;
    private Date created;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
