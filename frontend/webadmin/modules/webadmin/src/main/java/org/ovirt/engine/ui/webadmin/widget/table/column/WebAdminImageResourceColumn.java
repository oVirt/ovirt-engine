package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

public abstract class WebAdminImageResourceColumn<T> extends ImageResourceColumn<T> {

    protected ApplicationResources getApplicationResources() {
        return ClientGinjectorProvider.getApplicationResources();
    }

}
