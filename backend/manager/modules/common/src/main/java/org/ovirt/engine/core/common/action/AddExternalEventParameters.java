package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;

public class AddExternalEventParameters extends ActionParametersBase {
    private static final long serialVersionUID = 7023971593753015624L;

    private AuditLog event;
    private ExternalStatus externalStatus;

    public AuditLog getEvent() {
        return event;
    }

    public ExternalStatus getExternalStatus() {
        return externalStatus;
    }

    public void setExternalStatus(ExternalStatus externalStatus) {
        this.externalStatus = externalStatus;
    }

    public AddExternalEventParameters() {

    }
    public AddExternalEventParameters(AuditLog event) {
        super();
        this.event = event;
    }

    public AddExternalEventParameters(AuditLog event, ExternalStatus externalStatus) {
        this(event);
        this.externalStatus = externalStatus;
    }
}
