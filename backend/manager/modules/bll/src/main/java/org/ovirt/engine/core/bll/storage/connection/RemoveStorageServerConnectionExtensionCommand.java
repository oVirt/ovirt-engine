package org.ovirt.engine.core.bll.storage.connection;

import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;

public class RemoveStorageServerConnectionExtensionCommand<T extends IdParameters> extends StorageServerConnectionExtensionCommandBase<IdParameters> {

    private StorageServerConnectionExtension connExt;

    public RemoveStorageServerConnectionExtensionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        connExt = getConnectionExtension(getParameters().getId());
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean validate() {
        return validate(getConnectionExtensionValidator().isConnectionExtensionExists(getParameters().getId()));
    }

    @Override
    protected void executeCommand() {
        getDbFacade().getStorageServerConnectionExtensionDao().remove(getParameters().getId());
        getReturnValue().setSucceeded(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (connExt != null) {
            return createIdAndHostTargetLockMap(connExt);
        }
        else { // No point in locking, command will fail in CDA
            return null;
        }
    }
}
