package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.compat.Guid;

public class EventSubscriberId implements Serializable {
    private static final long serialVersionUID = 9035847334394545216L;

    Guid subscriberId;
    String eventUpName;
    EventNotificationMethod eventNotificationMethod;
    String tagName;

    @Override
    public int hashCode() {
        return Objects.hash(
                subscriberId,
                eventUpName,
                eventNotificationMethod,
                tagName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EventSubscriberId)) {
            return false;
        }
        EventSubscriberId other = (EventSubscriberId) obj;
        return Objects.equals(subscriberId, other.subscriberId)
                && Objects.equals(eventUpName, other.eventUpName)
                && eventNotificationMethod == other.eventNotificationMethod
                && Objects.equals(tagName, other.tagName);
    }
}
