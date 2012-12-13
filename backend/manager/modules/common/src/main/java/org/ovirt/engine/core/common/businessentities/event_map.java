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
public class event_map implements Serializable {
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

    public event_map() {
    }

    public int getevent_down_id() {
        return this.eventDownId;
    }

    public void setevent_down_id(int value) {
        this.eventDownId = value;
    }

    public int getevent_up_id() {
        return this.eventUpId;
    }

    public void setevent_up_id(int value) {
        this.eventUpId = value;
    }

    public String getevent_up_name() {
        return this.id.eventUpName;
    }

    public void setevent_up_name(String value) {
        this.id.eventUpName = value;
    }

    public String getevent_down_name() {
        return this.id.eventDownName;
    }

    public void setevent_down_name(String value) {
        this.id.eventDownName = value;
    }

}
