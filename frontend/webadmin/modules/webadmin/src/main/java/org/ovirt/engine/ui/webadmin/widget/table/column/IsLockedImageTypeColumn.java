package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.roles;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class IsLockedImageTypeColumn extends WebAdminImageResourceColumn<roles> {

    @Override
    public ImageResource getValue(roles role) {
        if (role.getis_readonly()) {
            return getApplicationResources().lockImage();
        }
        return null;
    }

}
