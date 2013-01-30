package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class EventMap implements Serializable {
    private static final long serialVersionUID = 3159004698645803750L;

    private EventMapId id = new EventMapId();

    private int eventDownId;

    private int eventUpId;

    public EventMap() {
    }

    public int getEventDownId() {
        return this.eventDownId;
    }

    public void setEventDownId(int value) {
        this.eventDownId = value;
    }

    public int getEventUpId() {
        return this.eventUpId;
    }

    public void setEventUpId(int value) {
        this.eventUpId = value;
    }

    public String getEventUpName() {
        return this.id.eventUpName;
    }

    public void setEventUpName(String value) {
        this.id.eventUpName = value;
    }

    public String getEventDownName() {
        return this.id.eventDownName;
    }

    public void setEventDownName(String value) {
        this.id.eventDownName = value;
    }

}
