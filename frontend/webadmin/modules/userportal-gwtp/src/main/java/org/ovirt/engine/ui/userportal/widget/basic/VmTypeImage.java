package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

/**
 * The type of VM such as server, desktop, pool
 *
 */
public class VmTypeImage extends AbstractDynamicImage<UserPortalItemModel> {

    @Override
    protected String imageName(UserPortalItemModel value) {
        if (value == null) {
            return defaultImageName(null);
        }
        if (value.isPool()) {
            return "poolVmIcon"; //$NON-NLS-1$
        } else if (value.getIsServer()) {
            return "serverVmIcon"; //$NON-NLS-1$
        }

        return "desktopVmIcon"; //$NON-NLS-1$
    }

    @Override
    protected String defaultImageName(UserPortalItemModel value) {
        return "desktopVmIcon"; //$NON-NLS-1$
    }

}
