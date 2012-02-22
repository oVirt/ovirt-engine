package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.BasePermissionTypeColumn;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

import com.google.gwt.cell.client.ImageResourceCell;

public class UserPortalPermissionTypeColumn extends BasePermissionTypeColumn {

    public UserPortalPermissionTypeColumn() {
        super(new ImageResourceCell(), ClientGinjectorProvider.instance().getApplicationResources());
    }
}
