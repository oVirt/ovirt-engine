package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ClusterUpgradeParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateClusterUpgradeCommand<T extends ClusterUpgradeParameters> extends ClusterCommandBase<T> {

    @Inject
    private ClusterDao clusterDao;

    private Cluster cluster;

    @Override
    protected void init() {
        cluster = clusterDao.get(getCluster().getId());
    }

    public UpdateClusterUpgradeCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected UpdateClusterUpgradeCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(() -> {
            int percentComplete = getParameters().getUpgradePercentComplete();
            boolean updated = clusterDao.setUpgradePercentComplete(cluster.getId(), percentComplete);

            if (updated) {
                log.debug("Set upgrade percent complete to {} on cluster '{}'.", percentComplete, cluster.getId());
            } else {
                String msg = String.format("Failed to set upgrade percent complete on cluster '%s'.", cluster.getId());
                log.error(msg);
                getReturnValue().getExecuteFailedMessages().add(msg);
            }
            setActionReturnValue(updated);
            setSucceeded(updated);
            return null;
        });
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded()
                ? AuditLogType.USER_UPDATE_CLUSTER_UPGRADE
                : AuditLogType.USER_UPDATE_CLUSTER_UPGRADE_FAILED;
    }

    @Override
    protected boolean validate() {
        boolean upgradeStarted = cluster.isUpgradeRunning();
        if (!upgradeStarted) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_UPGRADE_NOT_STARTED);
        }

        int percentComplete = getParameters().getUpgradePercentComplete();
        if (percentComplete < 0 || percentComplete > 100) {
            return failValidation(EngineMessage.ACTION_TYPE_CLUSTER_UPGRADE_PERCENT_COMPLETE_INVALID);
        }

        String currentCorrelationId = cluster.getUpgradeCorrelationId();
        String requestCorrelationId = getParameters().getEffectiveCorrelationid();
        if (!StringUtils.equals(currentCorrelationId, requestCorrelationId)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_UPGRADE_CORRELATION_MISMATCH);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }
}
