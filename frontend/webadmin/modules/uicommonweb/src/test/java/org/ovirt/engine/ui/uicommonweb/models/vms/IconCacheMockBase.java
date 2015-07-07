package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;


public abstract class IconCacheMockBase extends IconCache {

    public IconCacheMockBase() {
        super();
    }

    public void inject() {
        IconCache.setInstance(this);
    }

    public static void removeMock() {
        IconCache.setInstance(new IconCache());
    }

    @Override
    public void getOrFetchIcons(List<Guid> iconIds, IconsCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getIcon(Guid iconId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getOrFetchIcon(Guid iconId, IconCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Guid getId(String icon) {
        throw new UnsupportedOperationException();
    }
}
