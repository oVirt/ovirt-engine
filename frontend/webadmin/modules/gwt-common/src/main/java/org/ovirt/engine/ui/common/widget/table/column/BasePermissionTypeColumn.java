package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.CommonApplicationResources;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class BasePermissionTypeColumn extends BaseImageResourceColumn<permissions> {

    private final CommonApplicationResources resources;

    public BasePermissionTypeColumn(Cell<ImageResource> cell, CommonApplicationResources resources) {
        super(cell);
        this.resources = resources;
    }

    @Override
    public ImageResource getValue(permissions user) {
        switch (user.getRoleType()) {
        case ADMIN:
            return resources.adminImage();
        case USER:
            return resources.userImage();
        default:
            return resources.userImage();
        }
    }

}
