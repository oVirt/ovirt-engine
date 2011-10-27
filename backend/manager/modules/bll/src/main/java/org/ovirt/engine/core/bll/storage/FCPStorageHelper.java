package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class FCPStorageHelper extends StorageHelperBase {
    @Override
    public boolean ConnectStorageToDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return true;
    }

    @Override
    public boolean DisconnectStorageFromDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return true;
    }

    @Override
    protected boolean RunConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type) {
        return true;
    }

    @Override
    public boolean ConnectStorageToDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return true;
    }

    @Override
    public boolean DisconnectStorageFromDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return true;
    }

    @Override
    public boolean StorageDomainRemoved(storage_domain_static storageDomain) {
        List<LUNs> lunsList = DbFacade.getInstance().getLunDAO().getAllForVolumeGroup(storageDomain.getstorage());
        if (lunsList.size() != 0) {
            for (LUNs lun : lunsList) {
                DbFacade.getInstance().getLunDAO().remove(lun.getLUN_id());
            }
        }
        return true;
    }
}
