package org.ovirt.engine.core.common.eventqueue;

public class EventResult {

    private boolean success;
    private EventType eventType;

    public EventResult(boolean success, EventType eventType) {
        this.success = success;
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
