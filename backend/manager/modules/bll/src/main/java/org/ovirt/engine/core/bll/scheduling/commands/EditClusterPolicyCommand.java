package org.ovirt.engine.core.bll.scheduling.commands;

import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
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
        SchedulingManager.getInstance().editClusterPolicy(getClusterPolicy());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__CLUSTER_POLICY);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_CLUSTER_POLICY :
                AuditLogType.USER_FAILED_TO_UPDATE_CLUSTER_POLICY;
    }
}
