package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.BaseDiskImageStatusColumn;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

import com.google.gwt.cell.client.ImageResourceCell;

public class UserPortalDiskImageStatusColumn extends BaseDiskImageStatusColumn {

    public UserPortalDiskImageStatusColumn() {
        super(new ImageResourceCell(), ClientGinjectorProvider.instance().getApplicationResources());
    }

}
