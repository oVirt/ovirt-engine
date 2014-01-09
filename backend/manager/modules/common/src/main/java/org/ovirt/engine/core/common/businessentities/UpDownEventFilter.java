package org.ovirt.engine.core.common.businessentities;

/**
 * Note: UPDownEventFilters are immutable.
 */
public class UpDownEventFilter implements EventFilter {

    private String eventUpName;

    private String eventDownName;

    public void setEventUpName(String eventUpName) {
        this.eventUpName = eventUpName;
    }

    public void setEventDownName(String eventDownName) {
        this.eventDownName = eventDownName;
    }

    @Override
    public boolean isSubscribed(AuditLogEvent event) {
        return event.getLogTypeName().equals(eventUpName) || event.getLogTypeName().equals(eventDownName);
    }
}
