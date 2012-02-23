package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.CommonApplicationResources;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code HistorySeverityTemplate}.
 */
public class BaseAuditLogSeverityColumn extends BaseImageResourceColumn<AuditLog> {

    private final CommonApplicationResources resources;

    public BaseAuditLogSeverityColumn(Cell<ImageResource> cell, CommonApplicationResources resources) {
        super(cell);
        this.resources = resources;
    }

    @Override
    public ImageResource getValue(AuditLog log) {
        switch (log.getseverity()) {
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
