package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.NfsMountPointConstraint;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetStorageDomainStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainDynamicDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
@LockIdNameAttribute(isReleaseAtEndOfExecute = true)
public class UpdateStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends StorageServerConnectionCommandBase<T> {
    private List<StorageDomain> domains = null;

    public UpdateStorageServerConnectionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        StorageServerConnections newConnectionDetails = getParameters().getStorageServerConnection();

        if (newConnectionDetails.getstorage_type() != StorageType.NFS) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_STORAGE);
        }

        // todo change it to use annotation - in future patch
        // Check if the NFS path has a valid format
        if (!new NfsMountPointConstraint().isValid(newConnectionDetails.getconnection(), null)) {
            return failCanDoAction(VdcBllMessages.VALIDATION_STORAGE_CONNECTION_INVALID);
        }

        Guid vdsmId = getParameters().getVdsId();
        if (vdsmId == null || vdsmId.equals(Guid.Empty)) {
            return failCanDoAction(VdcBllMessages.VDS_EMPTY_NAME_OR_ID);
        }

        // Check if connection exists by id - otherwise there's nothing to update
        String connectionId = newConnectionDetails.getid();

        StorageServerConnections oldConnection = getStorageConnDao().get(connectionId);

        if (oldConnection == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        }

        if (!newConnectionDetails.getstorage_type().equals(oldConnection.getstorage_type())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_CHANGE_STORAGE_TYPE);
        }

        if (!oldConnection.getconnection().equals(newConnectionDetails.getconnection())) {
            // Check that there is no other connection with the new suggested path
            List<StorageServerConnections> connections =
                    getStorageConnDao().getAllForStorage(newConnectionDetails.getconnection());
            if (!connections.isEmpty()) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS);
            }
        }

        if (domains == null) {
            domains = getStorageDomainsByConnId(newConnectionDetails.getid());
        }
        if (domains.isEmpty()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }
        else if (domains.size() == 1) {
            setStorageDomain(domains.get(0));
        }
        else {
            String domainNames = createDomainNamesList(domains);
            addCanDoActionMessage(String.format("$domainNames %1$s", domainNames));
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
        }

        // Check that the storage domain is in proper state to be edited
        if (!isConnectionEditable(getStorageDomain())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_STORAGE);
        }

        return super.canDoAction();
    }

    protected String createDomainNamesList(List<StorageDomain> domains) {
        // Build domain names list to display in the error
        StringBuilder domainNames = new StringBuilder();
        for (StorageDomain domain : domains) {
            domainNames.append(domain.getStorageName());
            domainNames.append(",");
        }
        // Remove the last "," after the last domain
        domainNames.deleteCharAt(domainNames.length() - 1);
        return domainNames.toString();
    }

    protected boolean isConnectionEditable(StorageDomain storageDomain) {
        boolean isEditable =
                (storageDomain.getStorageDomainType() == StorageDomainType.Data || storageDomain.getStorageDomainType() == StorageDomainType.Master)
                        && storageDomain.getStatus() == StorageDomainStatus.Maintenance;
        return isEditable;
    }

    @Override
    protected void executeCommand() {
        StoragePoolIsoMap map = getStoragePoolIsoMap();

        changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Locked);
        // connect to the new path
        boolean hasConnectStorageSucceeded = connectToStorage();
        VDSReturnValue returnValueUpdatedStorageDomain = null;

        if (!hasConnectStorageSucceeded) {
            setSucceeded(false);
            VdcFault f = new VdcFault();
            f.setError(VdcBllErrors.StorageServerConnectionError);
            getReturnValue().setFault(f);
            return;
        }
        // update info such as free space - because we switched to a different server
        returnValueUpdatedStorageDomain = getStatsForDomain();

        if (returnValueUpdatedStorageDomain.getSucceeded()) {
            final StorageDomain updatedStorageDomain = (StorageDomain) returnValueUpdatedStorageDomain.getReturnValue();
            executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    getStorageConnDao().update(getParameters().getStorageServerConnection());
                    getStorageDomainDynamicDao().update(updatedStorageDomain.getStorageDynamicData());
                    return null;
                }
            });

            setSucceeded(true);
        }
        updateStatus(map, StorageDomainStatus.Maintenance);
        disconnectFromStorage();
    }

    protected StoragePoolIsoMap getStoragePoolIsoMap() {
        StoragePoolIsoMapId mapId = new StoragePoolIsoMapId(getStorageDomain().getId(),
                getParameters().getStoragePoolId());
        return getStoragePoolIsoMapDao().get(mapId);
    }

    protected void changeStorageDomainStatusInTransaction(final StoragePoolIsoMap map,
            final StorageDomainStatus status) {
        executeInNewTransaction(new TransactionMethod<StoragePoolIsoMap>() {
            @Override
            public StoragePoolIsoMap runInTransaction() {
                CompensationContext context = getCompensationContext();
                context.snapshotEntityStatus(map, map.getstatus());
                updateStatus(map, status);
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    protected void updateStatus(StoragePoolIsoMap map, StorageDomainStatus status) {
        map.setstatus(status);
        getStoragePoolIsoMapDao().updateStatus(map.getId(), map.getstatus());
    }

    protected void executeInNewTransaction(TransactionMethod<?> method) {
        TransactionSupport.executeInNewTransaction(method);
    }

    protected boolean connectToStorage() {
        ConnectStorageServerVDSCommandParameters newConnectionParametersForVdsm =
                createParametersForVdsm(getParameters().getVdsId(),
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageServerConnection().getstorage_type(),
                        getParameters().getStorageServerConnection());
        return runVdsCommand(VDSCommandType.ConnectStorageServer, newConnectionParametersForVdsm).getSucceeded();
    }

    protected void disconnectFromStorage() {
        ConnectStorageServerVDSCommandParameters connectionParametersForVdsm =
                createParametersForVdsm(getParameters().getVdsId(),
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageServerConnection().getstorage_type(),
                        getParameters().getStorageServerConnection());
        boolean isDisconnectSucceeded = runVdsCommand(VDSCommandType.DisconnectStorageServer, connectionParametersForVdsm).getSucceeded();
        if(!isDisconnectSucceeded) {
            log.warn("Failed to disconnect storage connection " + getParameters().getStorageServerConnection());
        }
    }

    protected VDSReturnValue getStatsForDomain() {
        return runVdsCommand(VDSCommandType.GetStorageDomainStats,
                new GetStorageDomainStatsVDSCommandParameters(getVds().getId(), getStorageDomain().getId()));
    }

    protected ConnectStorageServerVDSCommandParameters createParametersForVdsm(Guid vdsmId,
            Guid storagePoolId,
            StorageType storageType,
            StorageServerConnections storageServerConnection) {
        ConnectStorageServerVDSCommandParameters newConnectionParametersForVdsm =
                new ConnectStorageServerVDSCommandParameters(vdsmId, storagePoolId, storageType,
                        new ArrayList<StorageServerConnections>(Arrays
                                .asList(storageServerConnection)));
        return newConnectionParametersForVdsm;
    }

    protected StorageDomainDAO getStorageDomainDao() {
        return getDbFacade().getStorageDomainDao();
    }

    protected StorageServerConnectionDAO getStorageConnDao() {
        return getDbFacade().getStorageServerConnectionDao();
    }

    protected StorageDomainDynamicDAO getStorageDomainDynamicDao() {
        return getDbFacade().getStorageDomainDynamicDao();
    }

    protected StoragePoolIsoMapDAO getStoragePoolIsoMapDao() {
        return getDbFacade().getStoragePoolIsoMapDao();
    }

    protected List<StorageDomain> getStorageDomainsByConnId(String connectionId) {
        return getStorageDomainDao().getAllByConnectionId(Guid.createGuidFromString(connectionId));
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<String, Pair<String, String>>();
        domains = getStorageDomainsByConnId(getParameters().getStorageServerConnection().getid());
        if (!domains.isEmpty() && domains.size() == 1) {
            setStorageDomain(domains.get(0));
            locks.put(getStorageDomain().getId().toString(), LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                    VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        // lock the path to NFS to avoid at the same time if some other user tries to:
        // add new storage domain to same path or edit another storage server connection to point to same path
        locks.put(getParameters().getStorageServerConnection().getconnection(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return locks;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
    }
}
