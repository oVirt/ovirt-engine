package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;

import com.google.gwt.uibinder.client.UiConstructor;

/**
 * The type of VM such as server, desktop, pool
 *
 */
public class VmTypeImage extends AbstractDynamicImage<UserPortalItemModel, ApplicationResourcesWithLookup> {

    @UiConstructor
    public VmTypeImage(ApplicationResourcesWithLookup resources) {
        super(resources);
    }

    @Override
    protected String imageName(UserPortalItemModel value) {
        if (value == null) {
            return defaultImageName(value);
        }
        if (value.getIsPool()) {
            return "poolVmIcon";
        } else if (value.getIsServer()) {
            return "serverVmIcon";
        }

        return "desktopVmIcon";
    }

    @Override
    protected String defaultImageName(UserPortalItemModel value) {
        return "desktopVmIcon";
    }

}
