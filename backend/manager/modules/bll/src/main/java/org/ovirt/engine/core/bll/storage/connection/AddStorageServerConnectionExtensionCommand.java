package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.StorageServerConnectionExtensionParameters;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;

public class AddStorageServerConnectionExtensionCommand<T extends StorageServerConnectionExtensionParameters> extends StorageServerConnectionExtensionCommandBase<StorageServerConnectionExtensionParameters> {

    @Inject
    private StorageServerConnectionExtensionDao storageServerConnectionExtensionDao;

    public AddStorageServerConnectionExtensionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        setVdsId(getParameters().getStorageServerConnectionExtension().getHostId());
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
    }

    @Override
    protected boolean validate() {
        StorageServerConnectionExtension newConnExt = getParameters().getStorageServerConnectionExtension();
        return validate(getConnectionExtensionValidator().isConnectionDoesNotExistForHostAndTarget(newConnExt));
    }

    @Override
    protected void executeCommand() {
        storageServerConnectionExtensionDao.save(getParameters().getStorageServerConnectionExtension());
        getReturnValue().setActionReturnValue(getParameters().getStorageServerConnectionExtension().getId());
        getReturnValue().setSucceeded(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        StorageServerConnectionExtension connExt = getParameters().getStorageServerConnectionExtension();
        String lock = connExt.getHostId().toString() + connExt.getIqn();
        return Collections.singletonMap(lock,
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION_EXTENSION,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
