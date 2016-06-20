package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.compat.Guid;

public abstract class FenceAgentCommandBase extends CommandBase<FenceAgentCommandParameterBase> {

    public FenceAgentCommandBase(FenceAgentCommandParameterBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public FenceAgentCommandBase(Guid commandId) {
        super(commandId);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getClusterId(), VdcObjectType.VDS,
                getActionType().getActionGroup()));
    }

}
