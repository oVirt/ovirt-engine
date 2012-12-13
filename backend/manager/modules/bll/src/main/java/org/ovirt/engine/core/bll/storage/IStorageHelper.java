package org.ovirt.engine.core.bll.storage;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;

public interface IStorageHelper {
    boolean connectStorageToDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId);

    boolean DisconnectStorageFromDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId);

    boolean ConnectStorageToDomainByVdsId(storage_domains storageDomain, Guid vdsId);

    boolean DisconnectStorageFromDomainByVdsId(storage_domains storageDomain, Guid vdsId);

    boolean ConnectStorageToLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId);

    boolean DisconnectStorageFromLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun);

    boolean StorageDomainRemoved(StorageDomainStatic storageDomain);

    void removeLun(LUNs lun);

    boolean ValidateStoragePoolConnectionsInHost(VDS vds, List<StorageServerConnections> connections,
            Guid storagePoolId);

    List<StorageServerConnections> GetStorageServerConnectionsByDomain(
            StorageDomainStatic storageDomain);

    boolean IsConnectSucceeded(Map<String, String> returnValue,
            List<StorageServerConnections> connections);
}
