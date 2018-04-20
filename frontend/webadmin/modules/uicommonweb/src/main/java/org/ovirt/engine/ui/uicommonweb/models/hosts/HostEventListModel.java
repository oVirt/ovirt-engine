package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.events.SubTabEventListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class HostEventListModel extends SubTabEventListModel<VDS> {

    @Override
    protected void onEntityContentChanged() {
        super.onEntityContentChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        } else {
            setItems(null);
        }
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            setSearchString("events: host.name=" + getEntity().getName()); //$NON-NLS-1$
            super.search();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("vds_name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }
}
