package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.AuditLog;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code HistorySeverityTemplate}.
 */
public class AuditLogSeverityColumn extends ImageResourceColumn<AuditLog> {

    @Override
    public ImageResource getValue(AuditLog log) {
        switch (log.getseverity()) {
        case NORMAL:
            return getApplicationResources().logNormalImage();
        case WARNING:
            return getApplicationResources().logWarningImage();
        case ERROR:
            return getApplicationResources().logErrorImage();
        case ALERT:
        default:
            return getApplicationResources().alertConfigureImage();
        }
    }

}
