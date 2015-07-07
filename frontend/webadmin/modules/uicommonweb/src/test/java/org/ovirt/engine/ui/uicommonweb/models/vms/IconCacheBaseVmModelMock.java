package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;


public class IconCacheBaseVmModelMock extends IconCacheMockBase {

    protected Map<Guid, String> map = new HashMap<>();

    public IconCacheBaseVmModelMock put(Guid iconId, String iconData) {
        map.put(iconId, iconData);
        return this;
    }

    @Override
    public void getOrFetchIcons(List<Guid> iconIds, IconsCallback callback) {
        Map<Guid, String> result = new HashMap<>();
        for(Guid id : iconIds) {
            final String icon = map.get(id);
            if (icon == null) {
                throw new RuntimeException("Icon not found, id=" + id.toString()); //$NON-NLS-1$
            }
            result.put(id, icon);
        }
        callback.onSuccess(result);
    }

    @Override
    public void getOrFetchIcon(Guid iconId, IconCallback callback) {
        String result = map.get(iconId);
        if (result == null) {
            throw new RuntimeException("Icon not found, id=" + iconId.toString()); //$NON-NLS-1$
        }
        callback.onSuccess(result);
    }
}
