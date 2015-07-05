package org.ovirt.engine.core.bll.scheduling.commands;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;


public class EditAffinityGroupCommand extends AffinityGroupCRUDCommand {

    public EditAffinityGroupCommand(AffinityGroupCRUDParameters parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (getAffinityGroup() == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID);
        }
        if (!getParameters().getAffinityGroup().getClusterId().equals(getClusterId())) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_CANNOT_CHANGE_CLUSTER_ID);
        }
        if (!getAffinityGroup().getName().equals(getParameters().getAffinityGroup().getName()) &&
                getAffinityGroupDao().getByName(getParameters().getAffinityGroup().getName()) != null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_AFFINITY_GROUP_NAME_EXISTS);
        }
        return validateParameters();
    }

    @Override
    protected void executeCommand() {
        getAffinityGroupDao().update(getParameters().getAffinityGroup());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_AFFINITY_GROUP
                : AuditLogType.USER_FAILED_TO_UPDATE_AFFINITY_GROUP;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(EngineMessage.VAR__ACTION__UPDATE);
    }
}
