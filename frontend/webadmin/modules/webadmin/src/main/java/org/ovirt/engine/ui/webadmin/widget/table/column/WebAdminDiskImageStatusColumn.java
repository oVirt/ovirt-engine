package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.BaseDiskImageStatusColumn;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

public class WebAdminDiskImageStatusColumn extends BaseDiskImageStatusColumn {

    public WebAdminDiskImageStatusColumn() {
        super(new StyledImageResourceCell(), ClientGinjectorProvider.instance().getApplicationResources());
    }

}
