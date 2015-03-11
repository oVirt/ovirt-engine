package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
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
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_INUSE,
                    String.format("$clusters %1$s", clustersListIntoTokenizedString()));
        }
        return true;
    }

    private String clustersListIntoTokenizedString() {
        List<VDSGroup> attachedClustersList =
                SchedulingManager.getInstance().getClustersByClusterPolicyId(getParameters().getClusterPolicyId());
        List<String> clusterNamesList = new LinkedList<>();
        for (VDSGroup vdsGroup : attachedClustersList)
            clusterNamesList.add(vdsGroup.getName());
        return StringUtils.join(clusterNamesList, ',');
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
