package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Role;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class RoleTypeColumn extends AbstractWebAdminImageResourceColumn<Role> {

    @Override
    public ImageResource getValue(Role role) {
        switch (role.getType()) {
        case ADMIN:
            return getApplicationResources().adminImage();
        case USER:
            return getApplicationResources().userImage();
        }
        return getApplicationResources().adminImage();
    }

}
