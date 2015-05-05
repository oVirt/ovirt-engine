package org.ovirt.engine.core.bll.scheduling.commands;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;

public class EditClusterPolicyCommand extends ClusterPolicyCRUDCommand {

    public EditClusterPolicyCommand(ClusterPolicyCRUDParameters parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        return checkRemoveEditValidations() && checkAddEditValidations();
    }

    @Override
    protected void executeCommand() {
        schedulingManager.editClusterPolicy(getClusterPolicy());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__UPDATE);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__CLUSTER_POLICY);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_CLUSTER_POLICY :
                AuditLogType.USER_FAILED_TO_UPDATE_CLUSTER_POLICY;
    }
}
