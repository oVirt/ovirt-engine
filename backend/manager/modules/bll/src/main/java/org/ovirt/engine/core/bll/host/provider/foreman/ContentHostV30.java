package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;

public class ContentHostV30 implements Serializable {
    private static final long serialVersionUID = -2714992402945915589L;

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
