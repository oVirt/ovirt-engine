package org.ovirt.engine.core.common.eventqueue;

import java.util.Objects;

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
        return Objects.hash(
                domainId,
                eventType,
                storagePoolId,
                vdsId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Event)) {
            return false;
        }
        Event other = (Event) obj;
        return Objects.equals(domainId, other.domainId)
                && eventType == other.eventType
                && Objects.equals(storagePoolId, other.storagePoolId)
                && Objects.equals(vdsId, other.vdsId);
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
