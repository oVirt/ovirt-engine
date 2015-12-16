package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;

public class RemoveClusterPolicyCommand extends ClusterPolicyCRUDCommand {

    public RemoveClusterPolicyCommand(ClusterPolicyCRUDParameters parameters) {
        super(parameters);
    }

    @Override
    protected boolean validate() {
        if (!checkRemoveEditValidations()) {
            return false;
        }
        if (getVdsGroupDao().getClustersByClusterPolicyId(getParameters().getClusterPolicyId()).size() > 0) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_INUSE,
                    String.format("$clusters %1$s", clustersListIntoTokenizedString()));
        }
        return true;
    }

    private String clustersListIntoTokenizedString() {
        List<VDSGroup> attachedClustersList =
                getVdsGroupDao().getClustersByClusterPolicyId(getParameters().getClusterPolicyId());
        List<String> clusterNamesList = new LinkedList<>();
        for (VDSGroup vdsGroup : attachedClustersList)
            clusterNamesList.add(vdsGroup.getName());
        return StringUtils.join(clusterNamesList, ',');
    }

    @Override
    protected void executeCommand() {
        schedulingManager.removeClusterPolicy(getParameters().getClusterPolicyId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__CLUSTER_POLICY);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_CLUSTER_POLICY :
                AuditLogType.USER_FAILED_TO_REMOVE_CLUSTER_POLICY;
    }
}
