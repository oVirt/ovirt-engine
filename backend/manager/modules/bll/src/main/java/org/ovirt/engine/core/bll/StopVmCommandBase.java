package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.kubevirt.KubevirtMonitoring;
import org.ovirt.engine.core.bll.quota.QuotaClusterConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StopVmParametersBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StopVmCommandBase<T extends StopVmParametersBase> extends VmOperationCommandBase<T>
        implements QuotaVdsDependent, QuotaStorageDependent {
    private static final Logger log = LoggerFactory.getLogger(StopVmCommandBase.class);

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VmStaticDao vmStaticDao;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private KubevirtMonitoring kubevirt;

    private boolean suspendedVm;

    protected StopVmCommandBase(Guid guid) {
        super(guid);
    }

    protected StopVmCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected boolean getSuspendedVm() {
        return suspendedVm;
    }

    @Override
    protected boolean validate() {
        if (shouldSkipCommandExecutionCached()) {
            return true;
        }

        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (isVmDuringBackup() && !getParameters().isForceStop()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_BACKUP);
        }

        if (!getVm().isRunning() && getVm().getStatus() != VMStatus.Paused
                && getVm().getStatus() != VMStatus.NotResponding && getVm().getStatus() != VMStatus.Suspended) {
            return failValidation(
                    (getVm().getStatus().isHibernating() || getVm().getStatus() == VMStatus.RestoringState) ?
                            EngineMessage.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING
                            : EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_RUNNING);
        }

        return true;
    }

    protected void destroyVm() {
        boolean vmMigrating = getVm().getStatus() == VMStatus.MigratingFrom && getVm().getMigratingToVds() != null;
        setActionReturnValue(vmMigrating ? destroyMigratingVm() : destroyNonMigratingVm());
    }

    private VDSReturnValue destroyNonMigratingVm() {
        return runVdsCommand(VDSCommandType.DestroyVm, buildDestroyVmVDSCommandParameters(getVdsId()));
    }

    /**
     * Try to destroy the VM on the destination host first, otherwise the VM may end up Paused on the destination.
     * In case this attempt fails due to noVM, that's fine because the VM may already been destroyed there by the
     * monitoring thread when detecting that the migration failed.
     * In case the destroy attempt on the source host fails as well due to noVM, the user should be notified that
     * the VM could not be destroyed because it was no found. If we manage to destroy the VM on the source host
     * then that should be placed as the action return value.
     * In case the destroy attempt on the source host fails due to noVM but the destroy attempt on the destination
     * host was successful, use the return value of the latter.
     */
    private VDSReturnValue destroyMigratingVm() {
        // We must prevent VM monitoring from happening so it won't issue a migration rerun attempt
        getVmManager().lockVm();
        try {
            VDSReturnValue returnValueFromDestination = null;
            try {
                returnValueFromDestination = runVdsCommand(
                        VDSCommandType.DestroyVm,
                        buildDestroyVmVDSCommandParameters(getVm().getMigratingToVds()));
            } catch(EngineException e) {
                switch (e.getErrorCode()) {
                case noVM:
                    break;
                default:
                    throw e;
                }
            }

            VDSReturnValue returnValueFromSource = null;
            try {
                returnValueFromSource = runVdsCommand(
                        VDSCommandType.DestroyVm,
                        buildDestroyVmVDSCommandParameters(getVdsId()));
            } catch(EngineException e) {
                switch (e.getErrorCode()) {
                case noVM:
                    if (returnValueFromDestination == null) {
                        throw e;
                    }
                    break;
                default:
                    throw e;
                }
            }

            return returnValueFromSource != null ? returnValueFromSource : returnValueFromDestination;
        } finally {
            getVmManager().unlockVm();
        }
    }

    private DestroyVmVDSCommandParameters buildDestroyVmVDSCommandParameters(Guid vdsId) {
        DestroyVmVDSCommandParameters parameters = new DestroyVmVDSCommandParameters(vdsId, getVmId());
        parameters.setReason(getParameters().getStopReason());
        return parameters;
    }

    @Override
    protected void executeVmCommand() {
        if (getVm().getOrigin() == OriginType.KUBEVIRT) {
            kubevirt.stop(getVm());
            setSucceeded(true);
            return;
        }

        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));

        boolean snapshotContainsMemory = getActiveSnapshot().containsMemory();
        Guid snapshotMemoryDiskId = getActiveSnapshot().getMemoryDiskId();
        Guid snapshotMetadataDiskId = getActiveSnapshot().getMetadataDiskId();

        suspendedVm = getVm().getStatus() == VMStatus.Suspended;
        if (suspendedVm) {
            endVmCommand();
            setCommandShouldBeLogged(true);
        } else {
            super.executeVmCommand();
        }

        vmStaticDao.incrementDbGeneration(getVm().getId());

        if (snapshotContainsMemory) {
            removeMemoryDisks(snapshotMemoryDiskId, snapshotMetadataDiskId);
        }
    }

    @Override
    protected void endVmCommand() {
        setCommandShouldBeLogged(false);
        if (getVm() == null) {
            log.warn("VM is null, not performing full endAction");
            setSucceeded(true);
            return;
        }

        getVm().setStatus(VMStatus.Down);
        snapshotDao.removeMemoryFromActiveSnapshot(getVmId());
        vmDynamicDao.update(getVm().getDynamicData());
        setSucceeded(true);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        if (getVm().getQuotaId() != null && !Guid.Empty.equals(getVm().getQuotaId())
                && getQuotaManager().isVmStatusQuotaCountable(getVm().getStatus())) {
            list.add(new QuotaClusterConsumptionParameter(getVm().getQuotaId(),
                    QuotaConsumptionParameter.QuotaAction.RELEASE,
                    getVm().getClusterId(),
                    VmCpuCountHelper.isDynamicCpuTopologySet(getVm()) ?
                            getVm().getCurrentCoresPerSocket() * getVm().getCurrentSockets() :
                            getVm().getCpuPerSocket() * getVm().getNumOfSockets(),
                    getVm().getMemSizeMb()));
        }
        return list;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        if (!getVm().isStateless()) {
            return list;
        }
        //if runAsStateless
        for (DiskImage image : getVm().getDiskList()) {
            if (image.getQuotaId() != null) {
                list.add(new QuotaStorageConsumptionParameter(image.getQuotaId(),
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        image.getStorageIds().get(0), image.getActualSize()));
            }
        }
        return list;
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        //
    }

    @Override
    protected  boolean shouldSkipCommandExecution() {
        return getVm() != null && getVm().getStatus() == VMStatus.Down;
    }

    protected AuditLogType logCommandExecutionSkipped(String actionName) {
        addCustomValue("Action", actionName);
        addCustomValue("VmStatus", getVm().getStatus().name());
        return AuditLogType.VM_ALREADY_IN_REQUESTED_STATUS;
    }

    public String getOptionalReason() {
        String reason = getParameters().getStopReason();
        return reason != null && !reason.isEmpty() ? String.format(" (Reason: %s)", reason) : null;
    }
}
