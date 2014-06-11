package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public abstract class StorageRegisterEntityListModel extends SearchableListModel {

    public StorageRegisterEntityListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public StorageDomain getEntity() {
        return (StorageDomain) super.getEntity();
    }

    public void setEntity(StorageDomain value)
    {
        super.setEntity(value);
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }
}
