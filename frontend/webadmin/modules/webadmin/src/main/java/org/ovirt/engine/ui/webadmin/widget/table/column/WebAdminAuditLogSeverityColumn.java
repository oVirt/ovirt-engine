package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.BaseAuditLogSeverityColumn;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

public class WebAdminAuditLogSeverityColumn extends BaseAuditLogSeverityColumn {

    public WebAdminAuditLogSeverityColumn() {
        super(new StyledImageResourceCell(), ClientGinjectorProvider.instance().getApplicationResources());
    }

}
