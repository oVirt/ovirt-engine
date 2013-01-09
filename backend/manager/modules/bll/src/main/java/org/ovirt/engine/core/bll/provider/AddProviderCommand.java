package org.ovirt.engine.core.bll.provider;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;

public class AddProviderCommand<P extends ProviderParameters> extends CommandBase<P> {

    public AddProviderCommand(Guid commandId) {
        super(commandId);
    }

    public AddProviderCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getParameters().getProvider().setId(Guid.NewGuid());
        getDbFacade().getProviderDao().save(getParameters().getProvider());
        getReturnValue().setActionReturnValue(getParameters().getProvider().getId());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_STORAGE_POOL));
    }

}
