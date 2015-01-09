package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Permissions;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class PermissionTypeColumn extends AbstractImageResourceColumn<Permissions> {

    public PermissionTypeColumn() {
        makeSortable(new Comparator<Permissions>() {

            @Override
            public int compare(Permissions o1, Permissions o2) {
                if (getValue(o1).equals(getValue(o2))) {
                    return 0;
                } else {
                    return (getCommonResources().userImage().equals(getValue(o1))) ? -1 : 1;
                }
            }
        });
    }

    @Override
    public ImageResource getValue(Permissions user) {
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
