package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

@InternalCommandAttribute
public class AddStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends
        ConnectStorageToVdsCommand<T> {

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    public AddStorageServerConnectionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void executeCommand() {
        // Attempt to connect only if a host is given.
        // If not, just save the connection to the database
        if (!Guid.isNullOrEmpty(getParameters().getVdsId())) {
            Pair<Boolean, Integer> result = connectHostToStorage();
            boolean isValidConnection = result.getFirst();

            // Process failure
            if (!isValidConnection) {
                throw new EngineException(EngineError.forValue(result.getSecond()));
            }
        }

        StorageServerConnections connection = getConnection();
        connection.setId(Guid.newGuid().toString());
        saveConnection(connection);
        getReturnValue().setActionReturnValue(connection.getId());

        setSucceeded(true);
    }

    protected StorageServerConnections getConnectionFromDbById(String connectionId) {
        return storageServerConnectionDao.get(connectionId);
    }

    protected void saveConnection(StorageServerConnections connection) {
        storageServerConnectionDao.save(connection);
    }

    @Override
    protected boolean validate() {
        StorageServerConnections paramConnection = getConnection();
        // if an id was sent - it's not ok since only the backend should allocate ids
        if (StringUtils.isNotEmpty(paramConnection.getId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ID_NOT_EMPTY);
        }

        if (!isValidConnection(paramConnection)) {
            return false;
        }

        Guid storagePoolId = Guid.isNullOrEmpty(getParameters().getVdsId()) ? null : getVds().getStoragePoolId();
        String duplicateConnectionId = isConnWithSameDetailsExists(paramConnection, storagePoolId);

        if (!duplicateConnectionId.isEmpty() && !duplicateConnectionId.equalsIgnoreCase(paramConnection.getId())) {
            String storageDomainName = getStorageNameByConnectionId(duplicateConnectionId);
            addValidationMessageVariable("connectionId", duplicateConnectionId);
            addValidationMessageVariable("storageDomainName", storageDomainName);
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS);
        }

        // If a Guid is not supplied, we won't attempt to [dis]connect.
        // If one is supplied, [dis]connecting will be attempted, so we need to
        // validate that it's a valid VDS ID and that the VDS is up.
        if (!Guid.isNullOrEmpty(getParameters().getVdsId())) {
            if (getVds() == null) {
                return failValidation(EngineMessage.VDS_INVALID_SERVER_ID);
            }
            if (getVds().getStatus() != VDSStatus.Up) {
                return failValidation(EngineMessage.VDS_ADD_STORAGE_SERVER_STATUS_MUST_BE_UP);
            }
        }
        return true;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getConnection().getStorageType().isFileDomain()) {
            // lock the path to NFS to avoid at the same time if some other user tries to:
            // add new storage domain to same path or edit another storage server connection to point to same path
            return Collections.singletonMap(getParameters().getStorageServerConnection().getConnection(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        } else { // lock target details
            return Collections.singletonMap(getConnection().getConnection() + ";" + getConnection().getIqn() + ";"
                    + getConnection().getPort() + ";" + getConnection().getUserName(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__CONNECTION);
    }
}
