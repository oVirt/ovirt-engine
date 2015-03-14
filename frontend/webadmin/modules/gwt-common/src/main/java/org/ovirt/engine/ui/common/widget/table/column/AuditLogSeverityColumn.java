package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code HistorySeverityTemplate}.
 */
public class AuditLogSeverityColumn extends AbstractImageResourceColumn<AuditLog> {

    private final static CommonApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(AuditLog log) {
        switch (log.getSeverity()) {
        case NORMAL:
            return resources.logNormalImage();
        case WARNING:
            return resources.logWarningImage();
        case ERROR:
            return resources.logErrorImage();
        case ALERT:
        default:
            return resources.alertConfigureImage();
        }
    }

}
