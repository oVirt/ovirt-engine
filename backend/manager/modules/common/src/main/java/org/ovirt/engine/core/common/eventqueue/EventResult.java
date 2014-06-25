package org.ovirt.engine.core.common.eventqueue;

public class EventResult {

    private boolean success;
    private EventType eventType;
    private Object resultData;

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

    public Object getResultData() {
        return resultData;
    }

    public void setResultData(Object resultData) {
        this.resultData = resultData;
    }
}
