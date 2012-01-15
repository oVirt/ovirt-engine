package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageType;

@SuppressWarnings("unused")
public class FcpImportStorageModel extends ImportSanStorageModel
{
    @Override
    public StorageType getType()
    {
        return StorageType.FCP;
    }

    @Override
    protected String getListName() {
        return "FcpImportStorageModel";
    }
}
