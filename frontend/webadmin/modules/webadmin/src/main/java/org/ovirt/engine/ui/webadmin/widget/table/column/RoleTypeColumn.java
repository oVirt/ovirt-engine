package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.roles;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class RoleTypeColumn extends ImageResourceColumn<roles> {

    @Override
    public ImageResource getValue(roles role) {
        switch (role.getType()) {
        case ADMIN:
            return getApplicationResources().adminImage();
        case USER:
            return getApplicationResources().userImage();
        }
        return getApplicationResources().adminImage();
    }
}
