package org.ovirt.engine.core.bll.scheduling.commands;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class AddClusterPolicyCommand extends ClusterPolicyCRUDCommand {

    public AddClusterPolicyCommand(ClusterPolicyCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        return checkAddEditValidations();
    }

    @Override
    protected void executeCommand() {
        getClusterPolicy().setId(Guid.newGuid());
        schedulingManager.addClusterPolicy(getClusterPolicy());
        getReturnValue().setActionReturnValue(getClusterPolicy().getId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__CLUSTER_POLICY);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_CLUSTER_POLICY :
                AuditLogType.USER_FAILED_TO_ADD_CLUSTER_POLICY;
    }
}
