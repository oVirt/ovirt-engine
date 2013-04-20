package org.ovirt.engine.core.bll;

import java.util.Map.Entry;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * This command will try to migrate all the vds vms (if needed) and move the vds
 * to Non-Operational state
 */
@SuppressWarnings("serial")
@NonTransactiveCommandAttribute
public class SetNonOperationalVdsCommand<T extends SetNonOperationalVdsParameters> extends MaintenanceVdsCommand<T> {

    public SetNonOperationalVdsCommand(T parameters) {
        super(parameters);
        setStorageDomainId(parameters.getStorageDomainId());
    }

    /**
     * Note: it's ok that this method isn't marked as async command even though it triggers
     * migrations as sub-commands, because those migrations are executed as different jobs
     */
    @Override
    protected void executeCommand() {
        if (getParameters().getSaveToDb()) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(getVdsId(),
                                    VDSStatus.NonOperational,
                                    getParameters().getNonOperationalReason()));
        }

        // if host failed to recover, no point in sending migrate, as it would fail.
        if (getParameters().getNonOperationalReason() != NonOperationalReason.TIMEOUT_RECOVERING_FROM_CRASH) {
            orderListOfRunningVmsOnVds(getVdsId());
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    // migrate vms according to cluster migrateOnError option
                    switch (getVdsGroup().getMigrateOnError()) {
                    case YES:
                        MigrateAllVms(getExecutionContext());
                        break;
                    case HA_ONLY:
                        MigrateAllVms(getExecutionContext(), true);
                        break;
                    default:
                        break;
                    }
                }
            });
        }

        if (getParameters().getNonOperationalReason() == NonOperationalReason.NETWORK_UNREACHABLE) {
            log.errorFormat("Host '{0}' is set to Non-Operational, it is missing the following networks: '{1}'",
                    getVds().getName(), getParameters().getCustomLogValues().get("Networks"));
        }

        if (getParameters().getNonOperationalReason() == NonOperationalReason.VM_NETWORK_IS_BRIDGELESS) {
            log.errorFormat("Host '{0}' is set to Non-Operational, the following networks are implemented as non-VM instead of a VM networks: '{1}'",
                    getVds().getName(), getParameters().getCustomLogValues().get("Networks"));
        }

        setSucceeded(true);
    }

    @Override
    protected CommandContext createMigrateVmContext(ExecutionContext parentContext, VM vm) {
        return ExecutionHandler.createInternalJobContext();
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
            result = false;
        }
        return result;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        for (Entry<String, String> e : getParameters().getCustomLogValues().entrySet()) {
            addCustomValue(e.getKey(), e.getValue());
        }
        switch (getParameters().getNonOperationalReason()) {
        case NETWORK_UNREACHABLE:
            return (getSucceeded()) ? AuditLogType.VDS_SET_NONOPERATIONAL_NETWORK
                    : AuditLogType.VDS_SET_NONOPERATIONAL_FAILED;
        case STORAGE_DOMAIN_UNREACHABLE:
            return (getSucceeded()) ? AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN
                    : AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN_FAILED;
        case TIMEOUT_RECOVERING_FROM_CRASH:
            return AuditLogType.VDS_RECOVER_FAILED;
        case KVM_NOT_RUNNING:
            return AuditLogType.VDS_RUN_IN_NO_KVM_MODE;
        case VERSION_INCOMPATIBLE_WITH_CLUSTER:
            return AuditLogType.VDS_VERSION_NOT_SUPPORTED_FOR_CLUSTER;
        case VM_NETWORK_IS_BRIDGELESS:
            return AuditLogType.VDS_SET_NON_OPERATIONAL_VM_NETWORK_IS_BRIDGELESS;
        case GLUSTER_COMMAND_FAILED:
            return AuditLogType.GLUSTER_COMMAND_FAILED;
        default:
            return (getSucceeded()) ? AuditLogType.VDS_SET_NONOPERATIONAL : AuditLogType.VDS_SET_NONOPERATIONAL_FAILED;
        }
    }
}
