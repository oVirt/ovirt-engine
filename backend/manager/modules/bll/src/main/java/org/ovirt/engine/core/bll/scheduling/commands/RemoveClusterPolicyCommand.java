package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.dao.ClusterDao;

public class RemoveClusterPolicyCommand extends ClusterPolicyCRUDCommand {

    @Inject
    private ClusterDao clusterDao;

    public RemoveClusterPolicyCommand(ClusterPolicyCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (!checkRemoveEditValidations()) {
            return false;
        }
        if (clusterDao.getClustersByClusterPolicyId(getParameters().getClusterPolicyId()).size() > 0) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_INUSE,
                    String.format("$clusters %1$s", clustersListIntoTokenizedString()));
        }
        return true;
    }

    private String clustersListIntoTokenizedString() {
        List<Cluster> attachedClustersList =
                clusterDao.getClustersByClusterPolicyId(getParameters().getClusterPolicyId());
        List<String> clusterNamesList = new LinkedList<>();
        for (Cluster cluster : attachedClustersList) {
            clusterNamesList.add(cluster.getName());
        }
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
