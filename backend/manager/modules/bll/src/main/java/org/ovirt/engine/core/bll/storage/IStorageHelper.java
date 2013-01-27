package org.ovirt.engine.core.bll.storage;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;

public interface IStorageHelper {

    boolean connectStorageToDomainByVdsId(storage_domains storageDomain, Guid vdsId);

    boolean disconnectStorageFromDomainByVdsId(storage_domains storageDomain, Guid vdsId);

    boolean connectStorageToLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId);

    boolean disconnectStorageFromLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun);

    boolean storageDomainRemoved(StorageDomainStatic storageDomain);

    void removeLun(LUNs lun);

    List<StorageServerConnections> getStorageServerConnectionsByDomain(
            StorageDomainStatic storageDomain);

    boolean isConnectSucceeded(Map<String, String> returnValue,
            List<StorageServerConnections> connections);
}
