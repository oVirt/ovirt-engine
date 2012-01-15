package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageType;

@SuppressWarnings("unused")
public class IscsiStorageModel extends SanStorageModel
{
    @Override
    public StorageType getType()
    {
        return StorageType.ISCSI;
    }

    @Override
    protected String getListName() {
        return "IscsiStorageModel";
    }
}
