package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class event_map_id implements Serializable {
    private static final long serialVersionUID = -643444133261195061L;

    public String eventDownName;

    public String eventUpName;

    public event_map_id() {
    }

    public event_map_id(String eventDownName, String eventUpName) {
        super();
        this.eventDownName = eventDownName;
        this.eventUpName = eventUpName;
    }
}
