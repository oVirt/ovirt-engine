package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * This command will try to migrate all the vds vms (if needed) and move the vds
 * to Non-Operational state
 */
@SuppressWarnings("serial")
@NonTransactiveCommandAttribute
public class SetNonOperationalVdsCommand<T extends SetNonOperationalVdsParameters> extends MaintenanceVdsCommand<T> {

    @Inject
    private GlusterBrickDao glusterBrickDao;

    public SetNonOperationalVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStorageDomainId(parameters.getStorageDomainId());
    }


    /**
     * Note: it's ok that this method isn't marked as async command even though it triggers
     * migrations as sub-commands, because those migrations are executed as different jobs
     */
    @Override
    protected void executeCommand() {
        setVdsStatus(VDSStatus.NonOperational, getParameters().getNonOperationalReason());
        if (getCluster() != null && getCluster().supportsGlusterService()) {
            updateBrickStatusDown();
        }

        // if host failed to recover, no point in sending migrate, as it would fail.
        if (getParameters().getNonOperationalReason() != NonOperationalReason.TIMEOUT_RECOVERING_FROM_CRASH) {
            orderListOfRunningVmsOnVds(getVdsId());
            ThreadPoolUtil.execute(() -> {
                // migrate vms according to cluster migrateOnError option
                switch (getCluster().getMigrateOnError()) {
                case YES:
                    migrateAllVms(getExecutionContext());
                    break;
                case HA_ONLY:
                    migrateAllVms(getExecutionContext(), true);
                    break;
                default:
                    break;
                }
            });
        }

        if (getParameters().getNonOperationalReason() == NonOperationalReason.NETWORK_UNREACHABLE) {
            log.error("Host '{}' is set to Non-Operational, it is missing the following networks: '{}'",
                    getVds().getName(), getParameters().getCustomLogValues().get("Networks"));
        }

        if (getParameters().getNonOperationalReason() == NonOperationalReason.VM_NETWORK_IS_BRIDGELESS) {
            log.error("Host '{}' is set to Non-Operational, the following networks are implemented as non-VM"
                            + " instead of a VM networks: '{}'",
                    getVds().getName(), getParameters().getCustomLogValues().get("Networks"));
        }

        setSucceeded(true);
    }

    private void updateBrickStatusDown() {
        List<GlusterBrickEntity> brickEntities = glusterBrickDao.getGlusterVolumeBricksByServerId(getVdsId());
        for (GlusterBrickEntity brick : brickEntities) {
            brick.setStatus(GlusterStatus.DOWN);
        }
        glusterBrickDao.updateBrickStatuses(brickEntities);
    }

    @Override
    protected CommandContext createMigrateVmsContext(ExecutionContext parentContext) {
        return ExecutionHandler.createInternalJobContext(getContext());
    }

    @Override
    protected boolean validate() {
        if (getVds() == null) {
            return failValidation(EngineMessage.VDS_INVALID_SERVER_ID);
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        for (Entry<String, String> e : getParameters().getCustomLogValues().entrySet()) {
            addCustomValue(e.getKey(), e.getValue());
        }
        switch (getParameters().getNonOperationalReason()) {
        case NETWORK_UNREACHABLE:
            return getSucceeded() ? AuditLogType.VDS_SET_NONOPERATIONAL_NETWORK
                    : AuditLogType.VDS_SET_NONOPERATIONAL_FAILED;
        case STORAGE_DOMAIN_UNREACHABLE:
            return getSucceeded() ? AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN
                    : AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN_FAILED;
        case TIMEOUT_RECOVERING_FROM_CRASH:
            return AuditLogType.VDS_RECOVER_FAILED;
        case KVM_NOT_RUNNING:
            return AuditLogType.VDS_RUN_IN_NO_KVM_MODE;
        case VERSION_INCOMPATIBLE_WITH_CLUSTER:
            return AuditLogType.VDS_VERSION_NOT_SUPPORTED_FOR_CLUSTER;
        case CLUSTER_VERSION_INCOMPATIBLE_WITH_CLUSTER:
            return AuditLogType.VDS_CLUSTER_VERSION_NOT_SUPPORTED;
        case CPU_TYPE_UNSUPPORTED_IN_THIS_CLUSTER_VERSION:
            return AuditLogType.CPU_TYPE_UNSUPPORTED_IN_THIS_CLUSTER_VERSION;
        case VM_NETWORK_IS_BRIDGELESS:
            return AuditLogType.VDS_SET_NON_OPERATIONAL_VM_NETWORK_IS_BRIDGELESS;
        case GLUSTER_COMMAND_FAILED:
            return AuditLogType.GLUSTER_COMMAND_FAILED;
        case GLUSTER_HOST_UUID_NOT_FOUND:
            return AuditLogType.GLUSTER_HOST_UUID_NOT_FOUND;
        case GLUSTER_HOST_UUID_ALREADY_EXISTS:
            return AuditLogType.GLUSTER_HOST_UUID_ALREADY_EXISTS;
        case EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER:
            return AuditLogType.EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER;
        case EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER_LEVEL:
                return AuditLogType.EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER_LEVEL;
        case RNG_SOURCES_INCOMPATIBLE_WITH_CLUSTER:
            return AuditLogType.RNG_SOURCES_INCOMPATIBLE_WITH_CLUSTER;
        case MIXING_RHEL_VERSIONS_IN_CLUSTER:
            return AuditLogType.MIXING_RHEL_VERSIONS_IN_CLUSTER;
        case UNTRUSTED:
            return AuditLogType.VDS_UNTRUSTED;
        case HOST_FEATURES_INCOMPATIBILE_WITH_CLUSTER:
            return AuditLogType.HOST_FEATURES_INCOMPATIBILE_WITH_CLUSTER;
        case LIBRBD_PACKAGE_NOT_AVAILABLE:
            return AuditLogType.NO_LIBRBD_PACKAGE_AVAILABLE_ON_VDS;
        case VDS_CANNOT_CONNECT_TO_GLUSTERFS:
            return AuditLogType.VDS_CANNOT_CONNECT_TO_GLUSTERFS;
        default:
            return getSucceeded() ? AuditLogType.VDS_SET_NONOPERATIONAL : AuditLogType.VDS_SET_NONOPERATIONAL_FAILED;
        }
    }
}
