package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

import java.util.List;

public interface IStorageHelper {
    boolean ConnectStorageToDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId);

    boolean DisconnectStorageFromDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId);

    boolean ConnectStorageToDomainByVdsId(storage_domains storageDomain, Guid vdsId);

    boolean DisconnectStorageFromDomainByVdsId(storage_domains storageDomain, Guid vdsId);

    boolean ConnectStorageToLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun);

    boolean DisconnectStorageFromLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun);

    boolean StorageDomainRemoved(storage_domain_static storageDomain);

    boolean ValidateStoragePoolConnectionsInHost(VDS vds, List<storage_server_connections> connections,
            Guid storagePoolId);

    List<storage_server_connections> GetStorageServerConnectionsByDomain(
            storage_domain_static storageDomain);

    boolean IsConnectSucceeded(java.util.HashMap<String, String> returnValue,
            List<storage_server_connections> connections);
}
