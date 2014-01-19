package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.compat.Guid;

public abstract class BaseIscsiBondCommand<T extends VdcActionParametersBase> extends CommandBase<T> {

    public BaseIscsiBondCommand(T parameters) {
        super(parameters);
    }

    public BaseIscsiBondCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }

    /**
     * Used by audit log to populate ${IscsiBondName} placeholder.
     * @return
     */
    public String getIscsiBondName() {
        return getIscsiBond().getName();
    }

    protected abstract IscsiBond getIscsiBond();
}
