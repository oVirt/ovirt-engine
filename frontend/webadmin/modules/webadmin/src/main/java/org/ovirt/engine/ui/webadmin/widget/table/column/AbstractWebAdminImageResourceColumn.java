package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

public abstract class AbstractWebAdminImageResourceColumn<T> extends AbstractImageResourceColumn<T> {

    protected ApplicationResources getApplicationResources() {
        return ClientGinjectorProvider.getApplicationResources();
    }

}
