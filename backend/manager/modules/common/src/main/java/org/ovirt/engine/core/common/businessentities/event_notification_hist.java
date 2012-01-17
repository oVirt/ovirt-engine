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

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "event_notification_hist")
@Entity
@Table(name = "event_notification_hist")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class event_notification_hist implements Serializable {
    private static final long serialVersionUID = 5812544412663001644L;

    public event_notification_hist() {
    }

    public event_notification_hist(long audit_log_id, String event_name, String method_type, String reason,
            java.util.Date sent_at, boolean status, Guid subscriber_id) {
        this.auditLogId = audit_log_id;
        this.eventName = event_name;
        this.methodType = method_type;
        this.reason = reason;
        this.sentAt = sent_at;
        this.status = status;
        this.subscriberId = subscriber_id;
    }

    @Column(name = "audit_log_id", nullable = false)
    private long auditLogId;

    @XmlElement
    public long getaudit_log_id() {
        return this.auditLogId;
    }

    public void setaudit_log_id(long value) {
        this.auditLogId = value;
    }

    @Column(name = "event_name", length = 100, nullable = false)
    private String eventName;

    @XmlElement
    public String getevent_name() {
        return this.eventName;
    }

    public void setevent_name(String value) {
        this.eventName = value;
    }

    @Column(name = "method_type", nullable = false)
    private String methodType;

    @XmlElement
    public String getmethod_type() {
        return this.methodType;
    }

    public void setmethod_type(String value) {
        this.methodType = value;
    }

    @Column(name = "reason", length = 255, nullable = false)
    private String reason;

    @XmlElement
    public String getreason() {
        return this.reason;
    }

    public void setreason(String value) {
        this.reason = value;
    }

    @Column(name = "sent_at", nullable = false)
    private java.util.Date sentAt = new java.util.Date(0);

    @XmlElement
    public java.util.Date getsent_at() {
        return this.sentAt;
    }

    public void setsent_at(java.util.Date value) {
        this.sentAt = value;
    }

    @Column(name = "status", nullable = false)
    private boolean status;

    @XmlElement
    public boolean getstatus() {
        return this.status;
    }

    public void setstatus(boolean value) {
        this.status = value;
    }

    @Id
    @Column(name = "subscriber_id", nullable = false)
    @Type(type = "guid")
    private Guid subscriberId = new Guid();

    @XmlElement
    public Guid getsubscriber_id() {
        return this.subscriberId;
    }

    public void setsubscriber_id(Guid value) {
        this.subscriberId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (auditLogId ^ (auditLogId >>> 32));
        result = prime * result + ((eventName == null) ? 0 : eventName.hashCode());
        result = prime * result + ((methodType == null) ? 0 : methodType.hashCode());
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + ((sentAt == null) ? 0 : sentAt.hashCode());
        result = prime * result + (status ? 1231 : 1237);
        result = prime * result + ((subscriberId == null) ? 0 : subscriberId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        event_notification_hist other = (event_notification_hist) obj;
        if (auditLogId != other.auditLogId)
            return false;
        if (eventName == null) {
            if (other.eventName != null)
                return false;
        } else if (!eventName.equals(other.eventName))
            return false;
        if (methodType == null) {
            if (other.methodType != null)
                return false;
        } else if (!methodType.equals(other.methodType))
            return false;
        if (reason == null) {
            if (other.reason != null)
                return false;
        } else if (!reason.equals(other.reason))
            return false;
        if (sentAt == null) {
            if (other.sentAt != null)
                return false;
        } else if (!sentAt.equals(other.sentAt))
            return false;
        if (status != other.status)
            return false;
        if (subscriberId == null) {
            if (other.subscriberId != null)
                return false;
        } else if (!subscriberId.equals(other.subscriberId))
            return false;
        return true;
    }
}
