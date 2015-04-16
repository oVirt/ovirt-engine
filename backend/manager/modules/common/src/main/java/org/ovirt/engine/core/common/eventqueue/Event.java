package org.ovirt.engine.core.common.eventqueue;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class Event {

    private EventType eventType;
    private Guid storagePoolId;
    private Guid domainId;
    private Guid vdsId;
    private String description;

    public Event(Guid storagePoolId, Guid domainId, Guid vdsId, EventType eventType, String description) {
        this.storagePoolId = storagePoolId;
        this.domainId = domainId;
        this.eventType = eventType;
        this.vdsId = vdsId;
        this.description = description;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public Guid getDomainId() {
        return domainId;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domainId == null) ? 0 : domainId.hashCode());
        result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + ((vdsId == null) ? 0 : vdsId.hashCode());
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
        Event other = (Event) obj;
        if (domainId == null) {
            if (other.domainId != null)
                return false;
        } else if (!domainId.equals(other.domainId))
            return false;
        if (eventType != other.eventType)
            return false;
        if (storagePoolId == null) {
            if (other.storagePoolId != null)
                return false;
        } else if (!storagePoolId.equals(other.storagePoolId))
            return false;
        if (vdsId == null) {
            if (other.vdsId != null)
                return false;
        } else if (!vdsId.equals(other.vdsId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("eventType",  eventType)
                .append("storagePoolId", storagePoolId)
                .append("domainId", domainId)
                .append("vdsId", vdsId)
                .append("description", description)
                .build();
    }
}
