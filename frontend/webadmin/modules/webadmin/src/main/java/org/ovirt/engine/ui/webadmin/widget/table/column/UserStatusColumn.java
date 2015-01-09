package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code UserStatusTemplate}.
 */
public class UserStatusColumn extends AbstractWebAdminImageResourceColumn<DbUser> {

    @Override
    public ImageResource getValue(DbUser user) {
        if (user.isAdmin()) {
            return getApplicationResources().adminImage();
        }
        if (user.isGroup()) {
            return getApplicationResources().userGroupImage();
        }
        return getApplicationResources().userImage();
    }

}
