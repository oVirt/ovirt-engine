package org.ovirt.engine.core.bll.storage;

import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.StorageServerConnectionExtensionValidator;
import org.ovirt.engine.core.common.action.StorageServerConnectionExtensionParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;

/**
 * Update a StorageServerConnectionExtension properties in the database only, if the specific host using this connection extension is already connected it will remain connected
 */
public class UpdateStorageServerConnectionExtensionCommand <T extends StorageServerConnectionExtensionParameters> extends StorageServerConnectionExtensionCommandBase<T> {

    public UpdateStorageServerConnectionExtensionCommand(T parameters) {
        super(parameters);
    }

    public UpdateStorageServerConnectionExtensionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    protected boolean canDoAction() {
        return validate(StorageServerConnectionExtensionValidator.isConnectionExtensionExists(getParameters().getStorageServerConnectionExtension().getId()));
    }

    @Override
    protected void executeCommand() {
        getDbFacade().getStorageServerConnectionExtensionDao().update(getParameters().getStorageServerConnectionExtension());
        getReturnValue().setSucceeded(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return createIdAndHostTargetLockMap(getParameters().getStorageServerConnectionExtension());
    }
}
