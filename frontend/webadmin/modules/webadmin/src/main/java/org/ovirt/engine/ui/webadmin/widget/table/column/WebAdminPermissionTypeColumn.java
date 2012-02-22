package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.BasePermissionTypeColumn;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

public class WebAdminPermissionTypeColumn extends BasePermissionTypeColumn {

    public WebAdminPermissionTypeColumn() {
        super(new StyledImageResourceCell(), ClientGinjectorProvider.instance().getApplicationResources());
    }

}
