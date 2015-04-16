package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconCache;

public class IconUtils {

    private IconUtils() {
    }

    public static boolean isCustom(String icon) {
        final Guid id = IconCache.getInstance().getId(icon);
        if (id == null) {
            return true;
        }
        return AsyncDataProvider.getInstance().isCustomIconId(id);
    }

    /**
     * @param icon icon in dataurl from
     * @return icon if icon is custom, null if icon is predefined
     */
    public static String filterPredefinedIcons(String icon) {
        return isCustom(icon)
                ? icon
                : null;
    }
}
