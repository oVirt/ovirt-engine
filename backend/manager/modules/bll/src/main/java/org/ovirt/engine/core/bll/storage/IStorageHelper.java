package org.ovirt.engine.core.bll.storage;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Guid;

public interface IStorageHelper {
    boolean ConnectStorageToDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId);

    boolean DisconnectStorageFromDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId);

    boolean ConnectStorageToDomainByVdsId(storage_domains storageDomain, Guid vdsId);

    boolean DisconnectStorageFromDomainByVdsId(storage_domains storageDomain, Guid vdsId);

    boolean ConnectStorageToLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId);

    boolean DisconnectStorageFromLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun);

    boolean StorageDomainRemoved(StorageDomainStatic storageDomain);

    void removeLun(LUNs lun);

    boolean ValidateStoragePoolConnectionsInHost(VDS vds, List<storage_server_connections> connections,
            Guid storagePoolId);

    List<storage_server_connections> GetStorageServerConnectionsByDomain(
            StorageDomainStatic storageDomain);

    boolean IsConnectSucceeded(Map<String, String> returnValue,
            List<storage_server_connections> connections);
}
