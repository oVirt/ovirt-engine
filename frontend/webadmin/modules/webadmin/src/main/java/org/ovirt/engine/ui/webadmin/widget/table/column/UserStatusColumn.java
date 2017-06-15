package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Image column that corresponds to XAML {@code UserStatusTemplate}.
 */
public class UserStatusColumn extends AbstractImageResourceColumn<DbUser> {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public ImageResource getValue(DbUser user) {
        if (user.isAdmin()) {
            return resources.adminImage();
        }
        if (user.isGroup()) {
            return resources.userGroupImage();
        }
        return resources.userImage();
    }

    @Override
    public SafeHtml getTooltip(DbUser user) {
        if (user.isAdmin()) {
            return SafeHtmlUtils.fromSafeConstant(constants.admin());
        }
        if (user.isGroup()) {
            return SafeHtmlUtils.fromSafeConstant(constants.group());
        }
        return SafeHtmlUtils.fromSafeConstant(constants.user());
    }

}
