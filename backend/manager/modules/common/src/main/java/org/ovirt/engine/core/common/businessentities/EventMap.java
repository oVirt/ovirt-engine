package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "event_map")
public class EventMap implements Serializable {
    private static final long serialVersionUID = 3159004698645803750L;

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "eventUpName", column = @Column(name = "event_up_name")),
            @AttributeOverride(name = "eventDownName", column = @Column(name = "event_down_name")) })
    private EventMapId id = new EventMapId();

    @Transient
    private int eventDownId;

    @Transient
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
