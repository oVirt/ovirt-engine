package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ImportFcpStorageModel extends ImportSanStorageModel {
    @Override
    public StorageType getType() {
        return StorageType.FCP;
    }

    public ImportFcpStorageModel() {
        setStorageDomains(new ListModel<>());
        getStorageDomains().setItems(new ArrayList<>());
    }

    @Override
    protected void update() {
        setMessage(null);
        getStorageDomains().setItems(new ArrayList<>());
        getUnregisteredStorageDomains(null);
    }

    @Override
    protected void postGetUnregisteredStorageDomains(List<StorageDomain> storageDomains, List<StorageServerConnections> connections) {
        setMessage(storageDomains == null || storageDomains.isEmpty() ?
                ConstantsManager.getInstance().getConstants().noStorageDomainsFound() : null);
    }
}
