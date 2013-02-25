package org.ovirt.engine.ui.uicommonweb.models.events;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class EventModel extends Model {

    private AuditLog event;

    public AuditLog getEvent() {
        return event;
    }

    public void setEvent(AuditLog event) {
        this.event = event;
    }

}
