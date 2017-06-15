package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StorageServerConnectionExtensionValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;

public abstract class StorageServerConnectionExtensionCommandBase<T extends ActionParametersBase> extends CommandBase<T> {

    @Inject
    private StorageServerConnectionExtensionDao storageServerConnectionExtensionDao;

    private StorageServerConnectionExtensionValidator connectionExtensionValidator = new StorageServerConnectionExtensionValidator();

    public StorageServerConnectionExtensionCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__CONNECTION__EXTENSION);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }

    protected StorageServerConnectionExtension getConnectionExtension(Guid connectionExtensionId) {
        return storageServerConnectionExtensionDao.get(connectionExtensionId);
    }

    protected Map<String, Pair<String, String>> createIdAndHostTargetLockMap(StorageServerConnectionExtension connExt) {
        Map<String, Pair<String, String>> lockMap = new HashMap<>();
        Pair<String, String> lockingPair = LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION_EXTENSION,
                EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED);

        String idLock = connExt.getId().toString();
        String hostTargetLock = connExt.getHostId().toString() + connExt.getIqn();

        lockMap.put(idLock, lockingPair);
        lockMap.put(hostTargetLock, lockingPair);
        return lockMap;
    }

    protected StorageServerConnectionExtensionValidator getConnectionExtensionValidator() {
        return connectionExtensionValidator;
    }
}
