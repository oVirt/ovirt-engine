package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.ClusterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class FinishClusterUpgradeCommand<T extends ClusterParametersBase> extends
        ClusterCommandBase<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ClusterDao clusterDao;

    private Cluster cluster;

    @Override
    protected void init() {
        cluster = clusterDao.get(getCluster().getId());
    }

    public FinishClusterUpgradeCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected FinishClusterUpgradeCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(() -> {
            boolean updated = clusterDao.clearUpgradeRunning(cluster.getId());
            if (updated) {
                log.info("Cleared upgrade running flag on Cluster '{}'.", cluster.getId());
            } else {
                log.warn("Failed to clear upgrade running flag on Cluster '{}'.", cluster.getId());
            }
            setSucceeded(updated);
            return null;
        });
    }


    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_FINISH_CLUSTER_UPGRADE
                : AuditLogType.USER_FINISH_CLUSTER_UPGRADE_FAILED;
    }

    @Override
    protected boolean validate() {
        ClusterValidator clusterValidator = getClusterValidator(cluster, getCluster());
        return validate(clusterValidator.oldClusterIsValid());
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }
}
