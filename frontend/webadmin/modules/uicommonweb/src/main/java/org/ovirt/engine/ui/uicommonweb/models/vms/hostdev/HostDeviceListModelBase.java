package org.ovirt.engine.ui.uicommonweb.models.vms.hostdev;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public abstract class HostDeviceListModelBase<E> extends SearchableListModel<E, HostDeviceView> {
    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public boolean supportsServerSideSorting() {
        return false;
    }
}
