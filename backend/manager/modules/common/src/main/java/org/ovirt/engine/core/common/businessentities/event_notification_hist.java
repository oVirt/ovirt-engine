package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class event_notification_hist implements Serializable {
    private static final long serialVersionUID = 5812544412663001644L;

    public event_notification_hist() {
        sentAt = new Date(0);
        subscriberId = Guid.Empty;
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

    private Date sentAt;

    public Date getsent_at() {
        return this.sentAt;
    }

    public void setsent_at(Date value) {
        this.sentAt = value;
    }

    private boolean status;

    public boolean getstatus() {
        return this.status;
    }

    public void setstatus(boolean value) {
        this.status = value;
    }

    private Guid subscriberId;

    public Guid getsubscriber_id() {
        return this.subscriberId;
    }

    public void setsubscriber_id(Guid value) {
        this.subscriberId = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                auditLogId,
                subscriberId,
                eventName,
                methodType,
                reason,
                sentAt,
                status
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof event_notification_hist)) {
            return false;
        }
        event_notification_hist other = (event_notification_hist) obj;
        return auditLogId == other.auditLogId
                && Objects.equals(subscriberId, other.subscriberId)
                && Objects.equals(eventName, other.eventName)
                && Objects.equals(methodType, other.methodType)
                && Objects.equals(reason, other.reason)
                && Objects.equals(sentAt, other.sentAt)
                && status == other.status;
    }
}
