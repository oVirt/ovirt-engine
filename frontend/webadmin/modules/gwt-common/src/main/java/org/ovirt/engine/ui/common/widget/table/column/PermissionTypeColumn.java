package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class PermissionTypeColumn extends AbstractImageResourceColumn<Permission> {

    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public PermissionTypeColumn() {
        makeSortable(Comparator.comparing(p -> !resources.userImage().equals(getValue(p))));
    }

    @Override
    public ImageResource getValue(Permission user) {
        switch (user.getRoleType()) {
        case ADMIN:
            return resources.adminImage();
        case USER:
            return resources.userImage();
        default:
            return resources.userImage();
        }
    }

    @Override
    public SafeHtml getTooltip(Permission user) {
        if (user.getRoleType() == RoleType.ADMIN) {
            return SafeHtmlUtils.fromSafeConstant(constants.admin());
        }
        return SafeHtmlUtils.fromSafeConstant(constants.user());
    }

}
