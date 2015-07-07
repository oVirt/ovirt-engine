package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class IconCacheModelVmBaseMock extends IconCacheMockBase {

    protected Map<String, Guid> map = new HashMap<>();

    public IconCacheModelVmBaseMock put(String iconData, Guid iconId) {
        map.put(iconData, iconId);
        return this;
    }

    @Override
    public Guid getId(String icon) {
        final Guid iconId = map.get(icon);
        if (iconId == null) {
            throw new RuntimeException("Icon not found in cache, iconUrl=" + icon); //$NON-NLS-1$
        }
        return iconId;
    }
}
