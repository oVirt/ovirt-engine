package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.utils.EnumUtils;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "event_notification_methods")
@Entity
@Table(name = "event_notification_methods")
public class event_notification_methods implements Serializable {
    private static final long serialVersionUID = -8648391842192154307L;

    public event_notification_methods() {
    }

    public event_notification_methods(int method_id, String method_type) {
        this.methodId = method_id;
        this.methodType = method_type;
    }

    @Id
    @Column(name = "method_id")
    private int methodId;

    @XmlElement
    public int getmethod_id() {
        return this.methodId;
    }

    public void setmethod_id(int value) {
        this.methodId = value;
    }

    @Column(name = "method_type", length = 10, nullable = false)
    private String methodType;

    @XmlElement
    public EventNotificationMethods getmethod_type() {
        return EnumUtils.valueOf(EventNotificationMethods.class, methodType, true);
    }

    public void setmethod_type(EventNotificationMethods value) {
        this.methodType = value.name();
    }

}
