package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;

@SuppressWarnings("unused")
public class FcpStorageModel extends SanStorageModelBase {
    @Override
    public StorageType getType() {
        return StorageType.FCP;
    }

    @Override
    protected String getListName() {
        return "FcpStorageModel"; //$NON-NLS-1$
    }
}
