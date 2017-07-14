package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public interface IStorageHelper {

    Collection<StorageType> getTypes();

    boolean prepareConnectHostToStoragePoolServers(CommandContext cmdContext,
            ConnectHostToStoragePoolServersParameters parameters,
            List<StorageServerConnections> connections);

    void prepareDisconnectHostFromStoragePoolServers(HostStoragePoolParametersBase parameters, List<StorageServerConnections> connections);

    Pair<Boolean, AuditLogType> disconnectHostFromStoragePoolServersCommandCompleted(HostStoragePoolParametersBase parameters);

    boolean connectStorageToDomainByVdsId(StorageDomain storageDomain, Guid vdsId);

    Pair<Boolean, EngineFault> connectStorageToDomainByVdsIdDetails(StorageDomain storageDomain, Guid vdsId);

    boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId);

    boolean connectStorageToLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId);

    boolean disconnectStorageFromLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun);

    boolean storageDomainRemoved(StorageDomainStatic storageDomain);

    /**
     * This method is called when a lun is no longer a part of a Storage Domain. If the lun is used by a disk its volume
     * group id field will be cleared, otherwise the lun will be deleted.
     * If the lun connection isn't used by other luns, it'll be removed as well.
     */
    void removeLunFromStorageDomain(String lunId);

    void removeLun(LUNs lun);

    boolean isConnectSucceeded(Map<String, String> returnValue,
            List<StorageServerConnections> connections);

    boolean syncDomainInfo(StorageDomain storageDomain, Guid vdsId);
}
