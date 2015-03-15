package org.ovirt.engine.core.bll.storage;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public interface IStorageHelper {

    boolean connectStorageToDomainByVdsId(StorageDomain storageDomain, Guid vdsId);

    Pair<Boolean, VdcFault> connectStorageToDomainByVdsIdDetails(StorageDomain storageDomain, Guid vdsId);

    boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId);

    boolean connectStorageToLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId);

    boolean disconnectStorageFromLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun);

    boolean storageDomainRemoved(StorageDomainStatic storageDomain);

    void removeLun(LUNs lun);

    List<StorageServerConnections> getStorageServerConnectionsByDomain(
            StorageDomainStatic storageDomain);

    boolean isConnectSucceeded(Map<String, String> returnValue,
            List<StorageServerConnections> connections);

    boolean syncDomainInfo(StorageDomain storageDomain, Guid vdsId);
}
