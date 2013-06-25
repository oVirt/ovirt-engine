package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

public abstract class UserPortalImageResourceColumn<T> extends ImageResourceColumn<T> {

    protected ApplicationResources getApplicationResources() {
        return ClientGinjectorProvider.getApplicationResources();
    }

    protected ApplicationResourcesWithLookup getApplicationResourcesWithLookup() {
        return ClientGinjectorProvider.getApplicationResourcesWithLookup();
    }

}
