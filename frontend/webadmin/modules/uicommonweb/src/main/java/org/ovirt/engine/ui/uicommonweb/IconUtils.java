package org.ovirt.engine.ui.uicommonweb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
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

    public static void prefetchIcons(Collection<VM> vmsAndPoolRepresentants,
            boolean smallIcons,
            boolean largeIcons,
            IconCache.IconsCallback callback) {
        final List<Guid> iconIdsToPrefetch = extractIconIds(vmsAndPoolRepresentants, smallIcons, largeIcons);
        IconCache.getInstance().getOrFetchIcons(iconIdsToPrefetch, callback);
    }

    private static List<Guid> extractIconIds(Collection<VM> vms, boolean smallIcons, boolean largeIcons) {
        final List<Guid> result = new ArrayList<>();
        for (VM vm: vms) {
            if (vm == null) {
                continue;
            }
            if (smallIcons) {
                result.add(vm.getStaticData().getSmallIconId());
            }
            if (largeIcons) {
                result.add(vm.getStaticData().getLargeIconId());
            }
        }
        return result;
    }
}
