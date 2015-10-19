package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsGroupConsumptionParameter;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StopVmParametersBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StopVmCommandBase<T extends StopVmParametersBase> extends VmOperationCommandBase<T>
        implements QuotaVdsDependent, QuotaStorageDependent {
    private static final Logger log = LoggerFactory.getLogger(StopVmCommandBase.class);

    private boolean suspendedVm;

    protected StopVmCommandBase(Guid guid) {
        super(guid);
    }

    protected StopVmCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setReason(parameters.getStopReason());
    }

    public StopVmCommandBase(T parameters) {
        this(parameters, null);
    }

    protected boolean getSuspendedVm() {
        return suspendedVm;
    }

    @Override
    protected boolean canDoAction() {
        if (shouldSkipCommandExecutionCached()) {
            return true;
        }

        if (getVm() == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!getVm().isRunning() && getVm().getStatus() != VMStatus.Paused
                && getVm().getStatus() != VMStatus.NotResponding && getVm().getStatus() != VMStatus.Suspended) {
            return failCanDoAction(
                    (getVm().getStatus().isHibernating() || getVm().getStatus() == VMStatus.RestoringState) ?
                            EngineMessage.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING
                            : EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_RUNNING);
        }

        return true;
    }

    protected void destroyVm() {
        if (getVm().getStatus() == VMStatus.MigratingFrom && getVm().getMigratingToVds() != null) {
            Backend.getInstance().getResourceManager().RunVdsCommand(
                    VDSCommandType.DestroyVm,
                    new DestroyVmVDSCommandParameters(getVm().getMigratingToVds(),
                            getVmId(), getParameters().getStopReason(), true, false, 0));
        }

        setActionReturnValue(Backend.getInstance().getResourceManager().RunVdsCommand(
                VDSCommandType.DestroyVm,
                new DestroyVmVDSCommandParameters(getVdsId(), getVmId(),
                        getParameters().getStopReason(), false, false, 0)).getReturnValue());
    }

    @Override
    protected void executeVmCommand() {
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));
        String hiberVol = getActiveSnapshot().getMemoryVolume();
        suspendedVm = getVm().getStatus() == VMStatus.Suspended;
        if (suspendedVm) {
            endVmCommand();
            setCommandShouldBeLogged(true);
        } else {
            super.executeVmCommand();
        }
        removeMemoryDisksIfNeeded(hiberVol);
    }

    private void removeMemoryDisksIfNeeded(String hiberVol) {
        if (StringUtils.isNotEmpty(hiberVol)) {
            removeHibernationDisks(hiberVol);
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
        getSnapshotDao().removeMemoryFromActiveSnapshot(getVmId());
        getVmDynamicDao().update(getVm().getDynamicData());
        setSucceeded(true);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        if (getVm().getQuotaId() != null && !Guid.Empty.equals(getVm().getQuotaId())
                && getQuotaManager().isVmStatusQuotaCountable(getVm().getStatus())) {
            list.add(new QuotaVdsGroupConsumptionParameter(getVm().getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.RELEASE,
                    getVm().getVdsGroupId(),
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
                list.add(new QuotaStorageConsumptionParameter(image.getQuotaId(), null,
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
}
