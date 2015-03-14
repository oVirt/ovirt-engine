package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class PermissionTypeColumn extends AbstractImageResourceColumn<Permission> {

    private final static CommonApplicationResources resources = AssetProvider.getResources();

    public PermissionTypeColumn() {
        makeSortable(new Comparator<Permission>() {

            @Override
            public int compare(Permission o1, Permission o2) {
                if (getValue(o1).equals(getValue(o2))) {
                    return 0;
                } else {
                    return (resources.userImage().equals(getValue(o1))) ? -1 : 1;
                }
            }
        });
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

}
