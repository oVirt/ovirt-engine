package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.DbUser;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code UserStatusTemplate}.
 */
public class UserStatusColumn extends ImageResourceColumn<DbUser> {

    @Override
    public ImageResource getValue(DbUser user) {
        if (user.getLastAdminCheckStatus()) {
            return getApplicationResources().adminImage();
        }
        if (user.getIsGroup()) {
            return getApplicationResources().userGroupImage();
        }
        return getApplicationResources().userImage();
    }

}
