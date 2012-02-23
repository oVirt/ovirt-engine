package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.BaseImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

public abstract class ImageResourceColumn<T> extends BaseImageResourceColumn<T> {

    public ImageResourceColumn() {
        super(new StyledImageResourceCell());
    }

    protected ApplicationResources getApplicationResources() {
        return ClientGinjectorProvider.instance().getApplicationResources();
    }

}
