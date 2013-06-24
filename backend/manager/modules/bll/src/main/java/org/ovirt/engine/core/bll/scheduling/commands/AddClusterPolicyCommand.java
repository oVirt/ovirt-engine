package org.ovirt.engine.core.bll.scheduling.commands;

import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class AddClusterPolicyCommand extends ClusterPolicyCRUDCommand {

    public AddClusterPolicyCommand(ClusterPolicyCRUDParameters parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (!checkAddEditValidations()) {
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        getClusterPolicy().setId(Guid.newGuid());
        SchedulingManager.getInstance().addClusterPolicy(getClusterPolicy());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__CLUSTER_POLICY);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_CLUSTER_POLICY :
                AuditLogType.USER_FAILED_TO_ADD_CLUSTER_POLICY;
    }
}
