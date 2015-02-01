package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;
import java.util.Date;

public class ContentHost implements Serializable {
    private static final long serialVersionUID = -6496042131555889764L;
    private String id;
    private Date created;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
