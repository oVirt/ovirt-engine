package org.ovirt.engine.ui.webadmin.widget.alert;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.uicommonweb.UICommand;

public interface AuditLogActionCallback {
    void executeCommand(UICommand command, AuditLog log);
}
