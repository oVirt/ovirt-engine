package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class EventMapId implements Serializable {
    private static final long serialVersionUID = -643444133261195061L;

    public String eventDownName;

    public String eventUpName;

    public EventMapId() {
    }

    public EventMapId(String eventDownName, String eventUpName) {
        super();
        this.eventDownName = eventDownName;
        this.eventUpName = eventUpName;
    }
}
