package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class ExternalHost implements Serializable {
    private static final long serialVersionUID = 468697212133957493L;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
