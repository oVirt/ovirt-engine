package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class EventNotificationHist implements Serializable {
    private static final long serialVersionUID = 5812544412663001644L;

    public EventNotificationHist() {
        sentAt = new Date(0);
        subscriberId = Guid.Empty;
    }

    private long auditLogId;

    public long getAuditLogId() {
        return this.auditLogId;
    }

    public void setAuditLogId(long value) {
        this.auditLogId = value;
    }

    private String eventName;

    public String getEventName() {
        return this.eventName;
    }

    public void setEventName(String value) {
        this.eventName = value;
    }

    private String methodType;

    public String getMethodType() {
        return this.methodType;
    }

    public void setMethodType(String value) {
        this.methodType = value;
    }

    private String reason;

    public String getReason() {
        return this.reason;
    }

    public void setReason(String value) {
        this.reason = value;
    }

    private Date sentAt;

    public Date getSentAt() {
        return this.sentAt;
    }

    public void setSentAt(Date value) {
        this.sentAt = value;
    }

    private boolean status;

    public boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean value) {
        this.status = value;
    }

    private Guid subscriberId;

    public Guid getSubscriberId() {
        return this.subscriberId;
    }

    public void setSubscriberId(Guid value) {
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
        if (!(obj instanceof EventNotificationHist)) {
            return false;
        }
        EventNotificationHist other = (EventNotificationHist) obj;
        return auditLogId == other.auditLogId
                && Objects.equals(subscriberId, other.subscriberId)
                && Objects.equals(eventName, other.eventName)
                && Objects.equals(methodType, other.methodType)
                && Objects.equals(reason, other.reason)
                && Objects.equals(sentAt, other.sentAt)
                && status == other.status;
    }
}
