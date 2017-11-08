package org.ovirt.engine.ui.webadmin.widget.alert;

import org.ovirt.engine.ui.uicommonweb.UICommand;

public interface ActionWidget {
    void addAction(String buttonLabel, UICommand command, AuditLogActionCallback callback);

    void addClearAllAction(String label, UICommand command, AuditLogActionCallback callback);

    void addRestoreAllAction(String label, UICommand command, AuditLogActionCallback callback);
}
