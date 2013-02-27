package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

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

    private long auditLogId;

    public long getaudit_log_id() {
        return this.auditLogId;
    }

    public void setaudit_log_id(long value) {
        this.auditLogId = value;
    }

    private String eventName;

    public String getevent_name() {
        return this.eventName;
    }

    public void setevent_name(String value) {
        this.eventName = value;
    }

    private String methodType;

    public String getmethod_type() {
        return this.methodType;
    }

    public void setmethod_type(String value) {
        this.methodType = value;
    }

    private String reason;

    public String getreason() {
        return this.reason;
    }

    public void setreason(String value) {
        this.reason = value;
    }

    private java.util.Date sentAt = new java.util.Date(0);

    public java.util.Date getsent_at() {
        return this.sentAt;
    }

    public void setsent_at(java.util.Date value) {
        this.sentAt = value;
    }

    private boolean status;

    public boolean getstatus() {
        return this.status;
    }

    public void setstatus(boolean value) {
        this.status = value;
    }

    private Guid subscriberId = new Guid();

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
        result = prime * result + ((subscriberId == null) ? 0 : subscriberId.hashCode());
        result = prime * result + ((eventName == null) ? 0 : eventName.hashCode());
        result = prime * result + ((methodType == null) ? 0 : methodType.hashCode());
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + ((sentAt == null) ? 0 : sentAt.hashCode());
        result = prime * result + (status ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        event_notification_hist other = (event_notification_hist) obj;
        return (auditLogId == other.auditLogId
                && ObjectUtils.objectsEqual(subscriberId, other.subscriberId)
                && ObjectUtils.objectsEqual(eventName, other.eventName)
                && ObjectUtils.objectsEqual(methodType, other.methodType)
                && ObjectUtils.objectsEqual(reason, other.reason)
                && ObjectUtils.objectsEqual(sentAt, other.sentAt)
                && status == other.status);
    }
}
