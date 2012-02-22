package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.BaseAuditLogSeverityColumn;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

import com.google.gwt.cell.client.ImageResourceCell;

public class UserPortalAuditLogSeverityColumn extends BaseAuditLogSeverityColumn {

    public UserPortalAuditLogSeverityColumn() {
        super(new ImageResourceCell(), ClientGinjectorProvider.instance().getApplicationResources());
    }

}
