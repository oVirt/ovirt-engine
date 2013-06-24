package org.ovirt.engine.core.bll.scheduling.commands;

import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;

public class RemoveClusterPolicyCommand extends ClusterPolicyCRUDCommand {

    public RemoveClusterPolicyCommand(ClusterPolicyCRUDParameters parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (!checkRemoveEditValidations()) {
            return false;
        }
        if (SchedulingManager.getInstance().getClustersByClusterPolicyId(getParameters().getClusterPolicyId()).size() > 0) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_PARAMETERS_INVALID);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        SchedulingManager.getInstance().removeClusterPolicy(getParameters().getClusterPolicyId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__CLUSTER_POLICY);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_CLUSTER_POLICY :
                AuditLogType.USER_FAILED_TO_REMOVE_CLUSTER_POLICY;
    }
}
