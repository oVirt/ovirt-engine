package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.uicommonweb.models.events.SubTabEventListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class ClusterEventListModel extends SubTabEventListModel<Cluster> {

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
            setSearchString("events: cluster=" + getEntity().getName()); //$NON-NLS-1$
            super.search();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }
}
