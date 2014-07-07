package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.compat.Guid;

public class FCPStorageHelper extends StorageHelperBase {

    @Override
    protected boolean runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        return true;
    }

    @Override
    public boolean connectStorageToDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return true;
    }

    @Override
    public boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return true;
    }

    @Override
    public boolean storageDomainRemoved(StorageDomainStatic storageDomain) {
        removeStorageDomainLuns(storageDomain);
        return true;
    }

    @Override
    public boolean syncDomainInfo(StorageDomain storageDomain, Guid vdsId) {
        // Synchronize LUN details comprising the storage domain with the DB
        StorageDomainParametersBase parameters = new StorageDomainParametersBase(storageDomain.getId());
        parameters.setVdsId(vdsId);
        return Backend.getInstance().runInternalAction(VdcActionType.SyncLunsInfoForBlockStorageDomain, parameters).getSucceeded();
    }
}
