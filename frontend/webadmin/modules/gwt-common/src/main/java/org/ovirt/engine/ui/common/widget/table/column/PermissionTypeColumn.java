package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.permissions;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class PermissionTypeColumn extends ImageResourceColumn<permissions> {

    @Override
    public ImageResource getValue(permissions user) {
        switch (user.getRoleType()) {
        case ADMIN:
            return getCommonResources().adminImage();
        case USER:
            return getCommonResources().userImage();
        default:
            return getCommonResources().userImage();
        }
    }

}
