package org.ovirt.engine.core.bll.scheduling.commands;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;


public class RemoveAffinityGroupCommand extends AffinityGroupCRUDCommand {

    public RemoveAffinityGroupCommand(AffinityGroupCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getAffinityGroup() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        getAffinityGroupDao().remove(getParameters().getAffinityGroupId());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVED_AFFINITY_GROUP
                : AuditLogType.USER_FAILED_TO_REMOVE_AFFINITY_GROUP;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }
}
