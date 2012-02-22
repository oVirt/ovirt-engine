package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.BaseAuditLogSeverityColumn;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

/**
 * Image column that corresponds to XAML {@code HistorySeverityTemplate}.
 */
public class WebadminAuditLogSeverityColumn extends BaseAuditLogSeverityColumn {

    public WebadminAuditLogSeverityColumn() {
        super(new StyledImageResourceCell(), ClientGinjectorProvider.instance().getApplicationResources());
    }

}
