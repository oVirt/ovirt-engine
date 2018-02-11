package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collection;
import java.util.Collections;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SyncLunsInfoForBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

public class FCPStorageHelper extends StorageHelperBase {

    private static final StorageServerConnections fcCon = new StorageServerConnections();

    static {
        fcCon.setId(Guid.Empty.toString());
        fcCon.setStorageType(StorageType.FCP);
    }

    @Override
    public Collection<StorageType> getTypes() {
        return Collections.singleton(StorageType.FCP);
    }

    @Override
    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        return runConnectionStorageToDomain(storageDomain, vdsId, type, null, Guid.Empty);
    }

    @Override
    public boolean connectStorageToDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.ConnectStorageServer.getValue()).getFirst();
    }

    @Override
    public boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.DisconnectStorageServer.getValue()).getFirst();
    }

    @Override
    public boolean connectStorageToLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId) {
        return runConnectionStorageToDomain(storageDomain,
                vdsId,
                VDSCommandType.ConnectStorageServer.getValue(),
                lun,
                storagePoolId).getFirst();
    }

    @Override
    public boolean storageDomainRemoved(StorageDomainStatic storageDomain) {
        removeStorageDomainLuns(storageDomain);
        return true;
    }

    @Override
    public boolean syncDomainInfo(StorageDomain storageDomain, Guid vdsId) {
        // Synchronize LUN details comprising the storage domain with the DB
        SyncLunsInfoForBlockStorageDomainParameters parameters = new SyncLunsInfoForBlockStorageDomainParameters(
                storageDomain.getId(), vdsId);
        return backend.runInternalAction(ActionType.SyncLunsInfoForBlockStorageDomain, parameters).getSucceeded();
    }

    public static StorageServerConnections getFCPConnection() {
        return fcCon;
    }

    @Override
    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain,
            Guid vdsId,
            int type,
            LUNs lun,
            Guid storagePoolId) {

        VDSReturnValue returnValue = resourceManager
                .runVdsCommand(
                        VDSCommandType.forValue(type),
                        new StorageServerConnectionManagementVDSParameters(vdsId,
                                storagePoolId, StorageType.FCP, Collections.singletonList(getFCPConnection())));
        boolean isSuccess = returnValue.getSucceeded();
        EngineFault engineFault = null;
        if (!isSuccess && returnValue.getVdsError() != null) {
            engineFault = new EngineFault();
            engineFault.setError(returnValue.getVdsError().getCode());
        }
        return new Pair<>(isSuccess, engineFault);
    }

    @Override
    public Pair<Boolean, EngineFault> connectStorageToDomainByVdsIdDetails(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.ConnectStorageServer.getValue());
    }

}
