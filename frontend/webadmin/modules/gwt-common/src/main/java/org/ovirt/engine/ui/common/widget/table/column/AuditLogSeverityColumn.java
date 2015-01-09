package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.AuditLog;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code HistorySeverityTemplate}.
 */
public class AuditLogSeverityColumn extends AbstractImageResourceColumn<AuditLog> {

    @Override
    public ImageResource getValue(AuditLog log) {
        switch (log.getSeverity()) {
        case NORMAL:
            return getCommonResources().logNormalImage();
        case WARNING:
            return getCommonResources().logWarningImage();
        case ERROR:
            return getCommonResources().logErrorImage();
        case ALERT:
        default:
            return getCommonResources().alertConfigureImage();
        }
    }

}
