package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class event_subscriber_id implements Serializable {
    private static final long serialVersionUID = 9035847334394545216L;

    Guid subscriberId;
    String eventUpName;
    int methodId;
    String tagName;

    public event_subscriber_id() {
    }

    public event_subscriber_id(Guid subscriberId, String eventUpName, int methodId, String tagName) {
        super();
        this.subscriberId = subscriberId;
        this.eventUpName = eventUpName;
        this.methodId = methodId;
        this.tagName = tagName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventUpName == null) ? 0 : eventUpName.hashCode());
        result = prime * result + methodId;
        result = prime * result + ((subscriberId == null) ? 0 : subscriberId.hashCode());
        result = prime * result + ((tagName == null) ? 0 : tagName.hashCode());
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
        event_subscriber_id other = (event_subscriber_id) obj;
        if (eventUpName == null) {
            if (other.eventUpName != null)
                return false;
        } else if (!eventUpName.equals(other.eventUpName))
            return false;
        if (methodId != other.methodId)
            return false;
        if (subscriberId == null) {
            if (other.subscriberId != null)
                return false;
        } else if (!subscriberId.equals(other.subscriberId))
            return false;
        if (tagName == null) {
            if (other.tagName != null)
                return false;
        } else if (!tagName.equals(other.tagName))
            return false;
        return true;
    }
}
