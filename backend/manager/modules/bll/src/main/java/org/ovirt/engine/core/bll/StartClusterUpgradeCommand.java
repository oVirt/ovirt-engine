package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.ClusterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class StartClusterUpgradeCommand<T extends ClusterParametersBase> extends
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

    public StartClusterUpgradeCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected StartClusterUpgradeCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(() -> {
            boolean updated = clusterDao.setUpgradeRunning(cluster.getId());
            if (updated) {
                log.info("Successfully set upgrade running flag on cluster '{}'.", cluster.getId());
            } else {
                String msg = String.format("Failed to set upgrade running flag on cluster '%s'.", cluster.getId());
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
        return getSucceeded() ? AuditLogType.USER_START_CLUSTER_UPGRADE
                : AuditLogType.USER_START_CLUSTER_UPGRADE_FAILED;
    }

    @Override
    protected boolean validate() {
        ClusterValidator clusterValidator = getClusterValidator(cluster, getCluster());

        List<String> volumeNamesWithLowSpace = clusterValidator.getLowDeviceSpaceVolumes();
        if (volumeNamesWithLowSpace != null && volumeNamesWithLowSpace.size() > 0) {
            AuditLogable auditLog = new AuditLogableImpl();
            auditLog.setClusterId(cluster.getId());
            auditLog.setClusterName(cluster.getName());
            auditLog.addCustomValue("VolumeNames",
                    volumeNamesWithLowSpace.stream().map(i -> i.toString()).collect(Collectors.joining(",")));
            auditLogDirector.log(auditLog, AuditLogType.HOST_DISK_RUNNING_LOW_SPACE);
            return false;
        }
        return validate(clusterValidator.oldClusterIsValid());
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }
}
