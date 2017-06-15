package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class RoleTypeColumn extends AbstractImageResourceColumn<Role> {

    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(Role role) {
        switch (role.getType()) {
        case ADMIN:
            return resources.adminImage();
        case USER:
            return resources.userImage();
        }
        return resources.adminImage();
    }

}
