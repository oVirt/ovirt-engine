package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * BLL command to refresh gluster self heal
 */
public class SyncHealClusterVolumesCommand extends GlusterCommandBase<ClusterParametersBase> {

    @Inject
    private GlusterSyncJob glusterSyncJob;

    private Cluster cluster;

    public SyncHealClusterVolumesCommand(ClusterParametersBase parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected void init() {
        super.setClusterId(getParameters().getClusterId());
        cluster = getCluster();
    }

    @Override
    protected void executeCommand() {
        try {
            glusterSyncJob.refreshSelfHealData(cluster);
        } catch (Exception e) {
            log.error("Error while refreshing Gluster self heal data of cluster '{}': {}",
                    cluster.getName(),
                    e.getMessage());
            addCustomValue("ClusterName", cluster.getName());
            auditLog(this, AuditLogType.GLUSTER_VOLUME_HEAL_REFRESH_FAILED);
            setSucceeded(false);
        }
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        if(cluster == null) {
            return failValidation(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
        }
        if (!cluster.supportsGlusterService()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_PROVISIONING_NOT_SUPPORTED_BY_CLUSTER);
        }
        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__SYNC);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME);
    }

}
