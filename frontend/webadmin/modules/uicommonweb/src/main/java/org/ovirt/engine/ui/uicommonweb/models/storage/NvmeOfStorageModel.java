package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;

@SuppressWarnings("unused")
public class NvmeOfStorageModel extends SanStorageModelBase {
    @Override
    public StorageType getType() {
        return StorageType.NVMEOF;
    }

    @Override
    protected String getListName() {
        return "NvmeOfStorageModel"; //$NON-NLS-1$
    }
}
