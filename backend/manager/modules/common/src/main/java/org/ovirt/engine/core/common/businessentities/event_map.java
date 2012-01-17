package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "event_map")
@Entity
@Table(name = "event_map")
public class event_map implements Serializable {
    private static final long serialVersionUID = 3159004698645803750L;

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "eventUpName", column = @Column(name = "event_up_name")),
            @AttributeOverride(name = "eventDownName", column = @Column(name = "event_down_name")) })
    private event_map_id id = new event_map_id();

    public event_map() {
    }

    public event_map(String event_down_name, String event_up_name) {
        this.id.eventDownName = event_down_name;
        this.id.eventUpName = event_up_name;
        try {
            this.eventUpId = EnumUtils.valueOf(AuditLogType.class, event_down_name, true).getValue();
            eventUpId = EnumUtils.valueOf(AuditLogType.class, event_up_name, true).getValue();
        } catch (RuntimeException e) {
            log.errorFormat("Could not find events {0},{1}\n{2}", event_down_name, event_up_name, e);
        }

    }

    @Transient
    private int eventDownId;

    @XmlElement
    public int getevent_down_id() {
        return this.eventDownId;
    }

    public void setevent_down_id(int value) {
        this.eventDownId = value;
    }

    @Transient
    private int eventUpId;

    @XmlElement
    public int getevent_up_id() {
        return this.eventUpId;
    }

    public void setevent_up_id(int value) {
        this.eventUpId = value;
    }

    @XmlElement
    public String getevent_up_name() {
        return this.id.eventUpName;
    }

    public void setevent_up_name(String value) {
        this.id.eventUpName = value;
    }

    @XmlElement
    public String getevent_down_name() {
        return this.id.eventDownName;
    }

    public void setevent_down_name(String value) {
        this.id.eventDownName = value;
    }

    private static LogCompat log = LogFactoryCompat.getLog(event_map.class);

}
