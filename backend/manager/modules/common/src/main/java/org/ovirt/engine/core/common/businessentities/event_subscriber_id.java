package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.compat.Guid;

public class event_subscriber_id implements Serializable {
    private static final long serialVersionUID = 9035847334394545216L;

    Guid subscriberId;
    String eventUpName;
    EventNotificationMethod eventNotificationMethod;
    String tagName;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subscriberId == null) ? 0 : subscriberId.hashCode());
        result = prime * result + ((eventUpName == null) ? 0 : eventUpName.hashCode());
        result = prime * result + ((eventNotificationMethod == null) ? 0 : eventNotificationMethod.hashCode());
        result = prime * result + ((tagName == null) ? 0 : tagName.hashCode());
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
        event_subscriber_id other = (event_subscriber_id) obj;
        return (Objects.equals(subscriberId, other.subscriberId)
                && Objects.equals(eventUpName, other.eventUpName)
                && eventNotificationMethod == other.eventNotificationMethod
                && Objects.equals(tagName, other.tagName));
    }
}
