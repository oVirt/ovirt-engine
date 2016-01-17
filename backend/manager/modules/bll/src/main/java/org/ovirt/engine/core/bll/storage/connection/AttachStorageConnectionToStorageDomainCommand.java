package org.ovirt.engine.core.bll.storage.connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.validator.storage.StorageConnectionValidator;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;

public class AttachStorageConnectionToStorageDomainCommand<T extends AttachDetachStorageConnectionParameters>
        extends StorageDomainCommandBase<T> {

    public AttachStorageConnectionToStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        StorageConnectionValidator storageConnectionValidator = createStorageConnectionValidator();

        if (!validate(storageConnectionValidator.isConnectionExists())
                || !validate(storageConnectionValidator.isDomainOfConnectionExistsAndInactive(getStorageDomain()))
                || !validate(storageConnectionValidator.isISCSIConnectionAndDomain(getStorageDomain()))) {
            return false;
        }
        if (storageConnectionValidator.isConnectionForISCSIDomainAttached(getStorageDomain())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_FOR_DOMAIN_ALREADY_EXISTS);
        }
        return true;
    }

    protected StorageConnectionValidator createStorageConnectionValidator() {
        String connectionId = getParameters().getStorageConnectionId();
        StorageServerConnections connection = getStorageServerConnectionDao().get(connectionId);

        return new StorageConnectionValidator(connection);
    }

    @Override
    protected void executeCommand() {
        // Create a dummy lun
        LUNs dummyLun = createDummyLun();

        // Create storage server connection mapping
        LUNStorageServerConnectionMap connectionMapRecord =
                new LUNStorageServerConnectionMap(dummyLun.getLUNId(), getParameters().getStorageConnectionId());

        List<StorageServerConnections> connectionsForDomain;

        if (getLunDao().get(dummyLun.getLUNId()) == null) {
            getLunDao().save(dummyLun);

            // Save connection maps when creating the dummy lun for the first time
            connectionsForDomain = getStorageServerConnectionDao().getAllForDomain(getStorageDomainId());
            for (StorageServerConnections connection : connectionsForDomain) {
                saveConnection(new LUNStorageServerConnectionMap(dummyLun.getLUNId(), connection.getId()));
            }
        }

        // Save new connection map
        saveConnection(connectionMapRecord);

        setSucceeded(true);
    }

    private void saveConnection(LUNStorageServerConnectionMap connectionMapRecord) {
        if (getStorageServerConnectionLunMapDao().get(connectionMapRecord.getId()) == null) {
            getStorageServerConnectionLunMapDao().save(connectionMapRecord);
        }
    }

    private LUNs createDummyLun() {
        final LUNs dummyLun = new LUNs();
        dummyLun.setLUNId(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX + getStorageDomainId());
        dummyLun.setVolumeGroupId(getStorageDomain().getStorage());
        return dummyLun;
    }

@Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        // lock connection's id to avoid removing this connection at the same time
        // by another user
        locks.put(getParameters().getStorageConnectionId(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return locks;
    }


    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ATTACH);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__CONNECTION);
    }

    protected StorageServerConnectionLunMapDao getStorageServerConnectionLunMapDao() {
        return getDbFacade().getStorageServerConnectionLunMapDao();
    }
}
