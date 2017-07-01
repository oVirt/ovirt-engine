package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.queries.GetVmIconsParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;

/**
 * Bidirectional map Guid &lt;-> String (icon id &lt;-> dataUri icon)
 */
public class IconCache {

    protected GuidIconBiDiMap cache = new GuidIconBiDiMap();

    private static class InstanceHolder {
        static IconCache instance = new IconCache();
    }

    /**
     * Access is protected because of tests.
     */
    protected IconCache() {
    }

    public static IconCache getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * For test purpose only.
     */
    protected static void setInstance(IconCache instance) {
        InstanceHolder.instance = instance;
    }

    /**
     * @param iconIds id of requested icon, should not be null, nor the item should be null
     * @param callback callback that receives the icon; The callback can be called both synchronously
     *                 (in case the icon is cached locally) and asynchronously (if a query to server
     *                 needs to be done)
     */
    public void getOrFetchIcons(final List<Guid> iconIds, final IconsCallback callback) {
        assertNotNull(iconIds);
        final Map<Guid, String> localResult = getIcons(iconIds);
        if (localResult != null) {
            callback.onSuccess(localResult);
        } else {
            Frontend.getInstance().runQuery(QueryType.GetVmIcons, GetVmIconsParameters.create(iconIds),
                    new AsyncQuery<QueryReturnValue>(returnValue -> {
                        Map<Guid, String> idToIconMap = returnValue.getReturnValue();
                        IconCache.this.cache.putAll(idToIconMap);
                        final Map<Guid, String> result = IconCache.this.getIcons(iconIds);
                        callback.onSuccess(result);
                    })
            );
        }

    }

    /**
     * @return cached icon or null if icon of given id is not cached
     */
    public String getIcon(Guid iconId) {
        return cache.getIcon(iconId);
    }

    /**
     * Sugar for {@link #getOrFetchIcons(List, IconCache.IconsCallback)}
     */
    public void getOrFetchIcon(final Guid iconId, final IconCallback callback) {
        getOrFetchIcons(Collections.singletonList(iconId), idToIconMap -> {
            final String icon = idToIconMap.get(iconId);
            callback.onSuccess(icon);
        });
    }

    private void assertNotNull(List list) {
        if (list == null) {
            throw new IllegalArgumentException("Argument should not be null."); //$NON-NLS-1$
        }
        for (Object item : list) {
            if (item == null) {
                throw new IllegalArgumentException("Argument should not contain null."); //$NON-NLS-1$
            }
        }
    }

    /**
     * @param iconIds requested icon ids
     * @return icon ids -> icon data uri
     */
    private Map<Guid, String> getIcons(List<Guid> iconIds) {
        Map<Guid, String> result = new HashMap<>();
        for (Guid iconId : iconIds) {
            final String cachedIcon = cache.getIcon(iconId);
            if (cachedIcon == null) {
                return null;
            }
            result.put(iconId, cachedIcon);
        }
        return result;
    }

    public Guid getId(String icon) {
        return cache.getId(icon);
    }

    public interface IconsCallback extends AsyncCallback<Map<Guid, String>> {
    }

    public interface IconCallback extends AsyncCallback<String> {
    }

    private static class GuidIconBiDiMap {

        private Map<Guid, String> map = new HashMap<>();
        private Map<String, Guid> reverseMap = new HashMap<>();

        public String getIcon(Guid id) {
            return map.get(id);
        }

        public Guid getId(String icon) {
            return reverseMap.get(icon);
        }

        public void put(Guid id, String icon) {
            if (id == null || icon == null) {
                throw new IllegalArgumentException("Neither 'id' nor 'icon' can be null."); //$NON-NLS-1$
            }
            map.put(id, icon);
            reverseMap.put(icon, id);
        }

        public void putAll(Map<Guid, String> idToIconMap) {
            for (Map.Entry<Guid, String> entry : idToIconMap.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }
}
