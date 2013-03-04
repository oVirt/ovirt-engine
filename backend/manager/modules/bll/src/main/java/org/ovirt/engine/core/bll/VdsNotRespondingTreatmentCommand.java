package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

@NonTransactiveCommandAttribute
public class VdsNotRespondingTreatmentCommand<T extends FenceVdsActionParameters> extends RestartVdsCommand<T> {
    /**
     * use this member to determine if fence failed but vms moved to unknown mode (for the audit log type)
     */
    private boolean _vmsMovedToUnknown;
    private final String RESTART = "Restart";

    public VdsNotRespondingTreatmentCommand(T parameters) {
        super(parameters);
    }

    /**
     * Create an executor which retries to find a proxy, since this command is automatic and we don't want it to fail
     * fast if no proxy is available, but to try a few times.
     *
     * @return The executor, which is used to check if a proxy is available for fence the host.
     */
    @Override
    protected FenceExecutor createExecutorForProxyCheck() {
        return new FenceExecutor(getVds(), getParameters().getAction());
    }

    /**
     * Only fence the host if the VDS is down, otherwise it might have gone back up until this command was executed. If
     * the VDS is not fenced then don't send an audit log event.
     */
    @Override
    protected void executeCommand() {
        setVds(null);
        if (getVds() != null && shouldVdsBeFenced()) {
            super.executeCommand();
        } else {
            setCommandShouldBeLogged(false);
            log.infoFormat("Host {0}({1}) not fenced since it's status is ok, or it doesn't exist anymore.",
                    getVdsName(), getVdsId());
        }
    }

    @Override
    protected void setStatus() {
    }

    @Override
    protected void HandleError() {
        MoveVMsToUnknown();
        // if fence failed on spm, move storage pool to non operational
        if (getVds().getSpmStatus() != VdsSpmStatus.None) {
            log.infoFormat("Fence failed on vds {0} which is spm of pool {1} - moving pool to non operational",
                    getVds().getName(), getVds().getStoragePoolId());
            Backend.getInstance().runInternalAction(
                    VdcActionType.SetStoragePoolStatus,
                    new SetStoragePoolStatusParameters(getVds().getStoragePoolId(), StoragePoolStatus.NotOperational,
                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM));
        }
        _vmsMovedToUnknown = true;
        log.errorFormat("Failed to run Fence script on vds:{0}, VMs moved to UnKnown instead.", getVdsName());
        if (!getVds().getpm_enabled()) {
            AlertIfPowerManagementOperationSkipped(RESTART);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? _vmsMovedToUnknown ? AuditLogType.VDS_RECOVER_FAILED_VMS_UNKNOWN
                : AuditLogType.VDS_RECOVER : AuditLogType.VDS_RECOVER_FAILED;
    }

    @Override
    protected void handleNonRespondingTreatmentFailure() {
        AlertIfPowerManagementOperationSkipped(RESTART);
    };

    /**
     * Determine if the status is legal for actually fence the VDS.
     *
     * @return <c>true</c> if the VDS should be fenced, otherwise <c>false</c>.
     */
    private boolean shouldVdsBeFenced() {
        boolean result;
        switch (getVds().getStatus()) {
        case Down:
        case InstallFailed:
        case Maintenance:
        case NonOperational:
        case NonResponsive:
            result = true;
            break;
        default:
            result = false;
            break;
        }
        return result;
    }

    private void MoveVMsToUnknown() {
        for (VM vm : getVmList()) {
            DestroyVmOnDestination(vm);
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SetVmStatus,
                            new SetVmStatusVDSCommandParameters(vm.getId(), VMStatus.Unknown));
            // log VM transition to unknown status
            AuditLogableBase logable = new AuditLogableBase();
            logable.setVmId(vm.getId());
            AuditLogDirector.log(logable, AuditLogType.VM_SET_TO_UNKNOWN_STATUS);
        }
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<String, String>();
            jobProperties.put(VdcObjectType.VDS.name().toLowerCase(),
                    (getVdsName() == null) ? "" : getVdsName());
        }
        return jobProperties;
    }
}
