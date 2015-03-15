package org.ovirt.engine.core.bll.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.validator.storage.StorageConnectionValidator;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDAO;

public class AttachStorageConnectionToStorageDomainCommand<T extends AttachDetachStorageConnectionParameters>
        extends StorageDomainCommandBase<T> {

    public AttachStorageConnectionToStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        StorageConnectionValidator storageConnectionValidator = createStorageConnectionValidator();

        if (!validate(storageConnectionValidator.isConnectionExists())
                || !validate(storageConnectionValidator.isDomainOfConnectionExistsAndInactive(getStorageDomain()))
                || !validate(storageConnectionValidator.isISCSIConnectionAndDomain(getStorageDomain()))) {
            return false;
        }
        if (storageConnectionValidator.isConnectionForISCSIDomainAttached(getStorageDomain())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_FOR_DOMAIN_ALREADY_EXISTS);
        }
        return true;
    }

    protected StorageConnectionValidator createStorageConnectionValidator() {
        String connectionId = getParameters().getStorageConnectionId();
        StorageServerConnections connection = getStorageServerConnectionDAO().get(connectionId);

        return new StorageConnectionValidator(connection);
    }

    @Override
    protected void executeCommand() {
        // Create a dummy lun
        LUNs dummyLun = createDummyLun();

        // Create storage server connection mapping
        LUN_storage_server_connection_map connectionMapRecord =
                new LUN_storage_server_connection_map(dummyLun.getLUN_id(), getParameters().getStorageConnectionId());

        List<StorageServerConnections> connectionsForDomain;

        if (getLunDao().get(dummyLun.getLUN_id()) == null) {
            getLunDao().save(dummyLun);

            // Save connection maps when creating the dummy lun for the first time
            connectionsForDomain = getStorageServerConnectionDAO().getAllForDomain(getStorageDomainId());
            for (StorageServerConnections connection : connectionsForDomain) {
                saveConnection(new LUN_storage_server_connection_map(dummyLun.getLUN_id(), connection.getid()));
            }
        }

        // Save new connection map
        saveConnection(connectionMapRecord);

        setSucceeded(true);
    }

    private void saveConnection(LUN_storage_server_connection_map connectionMapRecord) {
        if (getStorageServerConnectionLunMapDao().get(connectionMapRecord.getId()) == null) {
            getStorageServerConnectionLunMapDao().save(connectionMapRecord);
        }
    }

    private LUNs createDummyLun() {
        final LUNs dummyLun = new LUNs();
        dummyLun.setLUN_id(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX + getStorageDomainId());
        dummyLun.setvolume_group_id(getStorageDomain().getStorage());
        return dummyLun;
    }

@Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<String, Pair<String, String>>();
        // lock connection's id to avoid removing this connection at the same time
        // by another user
        locks.put(getParameters().getStorageConnectionId(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return locks;
    }


    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ATTACH);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__CONNECTION);
    }

    protected StorageServerConnectionLunMapDAO getStorageServerConnectionLunMapDao() {
        return getDbFacade().getStorageServerConnectionLunMapDao();
    }
}
