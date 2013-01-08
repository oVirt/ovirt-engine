package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.vdscommands.SnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@LockIdNameAttribute
public class CreateAllSnapshotsFromVmCommand<T extends CreateAllSnapshotsFromVmParameters> extends VmCommand<T> implements QuotaStorageDependent {

    private static final long serialVersionUID = -2407757772735253053L;
    List<DiskImage> selectedActiveDisks;

    protected CreateAllSnapshotsFromVmCommand(Guid commandId) {
        super(commandId);
    }

    public CreateAllSnapshotsFromVmCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());
        setSnapshotName(parameters.getDescription());
        setStoragePoolId(getVm().getStoragePoolId());
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Snapshot.name().toLowerCase(), getParameters().getDescription());
        }
        return jobProperties;
    }

    /**
     * Filter all allowed snapshot disks.
     * @return list of disks to be snapshot.
     */
    private List<DiskImage> getDisksList() {
        if (selectedActiveDisks == null) {
            selectedActiveDisks = ImagesHandler.filterImageDisks(DbFacade.getInstance().getDiskDao().getAllForVm(getVmId()),
                    false,
                    true);
        }
        return selectedActiveDisks;
    }

    private void incrementVmGeneration() {
        getVmStaticDAO().incrementDbGeneration(getVm().getId());
    }

    @Override
    protected void executeVmCommand() {
        Guid newActiveSnapshotId = Guid.NewGuid();
        Guid createdSnapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
        getParameters().setSnapshotType(determineSnapshotType());
        getParameters().setInitialVmStatus(getVm().getStatus());

        getSnapshotDao().updateId(createdSnapshotId, newActiveSnapshotId);

        setActionReturnValue(createdSnapshotId);

        if (getDisksList().isEmpty()) {
            getParameters().setTaskGroupSuccess(true);
            new SnapshotsManager().addSnapshot(createdSnapshotId,
                    getParameters().getDescription(),
                    SnapshotStatus.OK,
                    getParameters().getSnapshotType(),
                    getVm(),
                    true,
                    getCompensationContext());
            // at the moment there's no need to execute vdsm Snapshot command for diskless snapshots,
            // when support for ram snapshot will be introduced, this vdsm command should be executed
            // for diskless snapshots as well (currently executed within endSuccesfully() method.
            incrementVmGeneration();
        } else {
            new SnapshotsManager().addSnapshot(createdSnapshotId,
                    getParameters().getDescription(),
                    getParameters().getSnapshotType(),
                    getVm(),
                    getCompensationContext());

            freeLock();
            for (DiskImage image : getDisksList()) {
                ImagesActionsParametersBase tempVar = new ImagesActionsParametersBase(image.getImageId());
                tempVar.setDescription(getParameters().getDescription());
                tempVar.setSessionId(getParameters().getSessionId());
                tempVar.setQuotaId(image.getQuotaId());
                tempVar.setVmSnapshotId(newActiveSnapshotId);
                tempVar.setEntityId(getParameters().getEntityId());
                VdcActionType parentCommand = getParameters().getParentCommand() != VdcActionType.Unknown ? getParameters()
                        .getParentCommand() : VdcActionType.CreateAllSnapshotsFromVm;
                tempVar.setParentCommand(parentCommand);
                ImagesActionsParametersBase p = tempVar;

                VdcActionParametersBase parrentParamsForTask = getParametersForTask(parentCommand, getParameters());
                p.setParentParameters(parrentParamsForTask);
                getParameters().getImagesParameters().add(p);

                VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                                VdcActionType.CreateSnapshot,
                                p,
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

                if (vdcReturnValue.getSucceeded()) {
                    getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                } else {
                    throw new VdcBLLException(vdcReturnValue.getFault().getError(),
                            "CreateAllSnapshotsFromVmCommand::executeVmCommand: Failed to create snapshot!");
                }
            }
        }
        setSucceeded(true);
    }

    /**
     * @return For internal execution, return the type from parameters, otherwise return {@link SnapshotType#REGULAR}.
     */
    protected SnapshotType determineSnapshotType() {
        return isInternalExecution() ? getParameters().getSnapshotType() : SnapshotType.REGULAR;
    }

    @Override
    protected void endVmCommand() {
        // The following code must be executed in an inner transaction to make the changes visible
        // to the RunVm command that might occur afterwards
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                final boolean taskGroupSucceeded = getParameters().getTaskGroupSuccess();
                Guid createdSnapshotId =
                        getSnapshotDao().getId(getVmId(), getParameters().getSnapshotType(), SnapshotStatus.LOCKED);
                if (taskGroupSucceeded) {
                    getSnapshotDao().updateStatus(createdSnapshotId, SnapshotStatus.OK);

                    if (getParameters().getParentCommand() != VdcActionType.RunVm && getVm() != null && getVm().isStatusUp()
                            && getVm().getRunOnVds() != null) {
                        performLiveSnapshot(createdSnapshotId);
                    }
                } else {
                    revertToActiveSnapshot(createdSnapshotId);
                }

                incrementVmGeneration();

                endActionOnDisks();
                setSucceeded(taskGroupSucceeded);
                getReturnValue().setEndActionTryAgain(false);
                return null;
            }
        });

        // if the VM is HA + it went down during the snapshot creation process because of an error, run it
        // (during the snapshot creation process the VM couldn't be started (rerun))
        if (getVm() != null && getVm().isAutoStartup() && isVmDownUnintentionally() &&
                getParameters().getInitialVmStatus() != VMStatus.Down) {
            Backend.getInstance().runInternalAction(VdcActionType.RunVm,
                    new RunVmParams(getVmId()),
                    ExecutionHandler.createInternalJobContext());
        }
    }

    /**
     * returns true if the VM is down because of an error, otherwise false
     */
    private boolean isVmDownUnintentionally() {
        VM vm = getVmDAO().get(getVmId());
        return vm.getExitStatus() == VmExitStatus.Error && vm.isDown();
    }

    /**
     * Perform live snapshot on the host that the VM is running on. If the snapshot fails, and the error is
     * unrecoverable then the {@link CreateAllSnapshotsFromVmParameters#getTaskGroupSuccess()} will return false - which
     * will indicate that rollback of snapshot command should happen.
     *
     * @param createdSnapshotId
     *            Snapshot to revert to being active, in case of rollback.
     */
    protected void performLiveSnapshot(Guid createdSnapshotId) {
        try {
            TransactionSupport.executeInScope(TransactionScopeOption.Suppress, new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    List<Disk> pluggedDisks = VmRunHandler.getInstance().getPluggedDisks(getVm());
                    runVdsCommand(VDSCommandType.Snapshot,
                            new SnapshotVDSCommandParameters(getVm().getRunOnVds().getValue(),
                                    getVm().getId(),
                                    ImagesHandler.filterImageDisks(pluggedDisks, false, true)));
                    return null;
                }
            });
        } catch (VdcBLLException e) {
            if (e.getErrorCode() != VdcBllErrors.SNAPSHOT_FAILED) {
                throw e;
            }
            handleVdsLiveSnapshotFailure(e);
        }
    }

    private void handleVdsLiveSnapshotFailure(VdcBLLException e) {
        log.warnFormat("Wasn't able to live snpashot due to error: {0}. VM will still be configured to the new created snapshot",
                ExceptionUtils.getMessage(e));
        AddCustomValue("SnapshotName", getSnapshotName());
        AddCustomValue("VmName", getVmName());
        AuditLogDirector.log(this, AuditLogType.USER_CREATE_LIVE_SNAPSHOT_FINISHED_FAILURE);
    }

    /**
     * Return the given snapshot ID's snapshot to be the active snapshot. The snapshot with the given ID is removed
     * in the process.
     *
     * @param createdSnapshotId
     *            The snapshot ID to return to being active.
     */
    protected void revertToActiveSnapshot(Guid createdSnapshotId) {
        getSnapshotDao().remove(createdSnapshotId);
        getSnapshotDao().updateId(getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE), createdSnapshotId);
        setSucceeded(false);
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_CREATE_SNAPSHOT : AuditLogType.USER_FAILED_CREATE_SNAPSHOT;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_SUCCESS
                    : AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_FAILURE;
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        List<DiskImage> disksList = getDisksList();
        if (disksList.size() > 0) {
            VmValidator vmValidator = new VmValidator(getVm());
            result = validate(new SnapshotsValidator().vmNotDuringSnapshot(getVmId()))
                    && validate(vmValidator.vmNotDuringMigration())
                    && validate(vmValidator.vmNotRunningStateless())
                    && canDoSnapshot(getVm())
                    && ImagesHandler.PerformImagesChecks(getVm(),
                            getReturnValue().getCanDoActionMessages(),
                            getVm().getStoragePoolId(),
                            Guid.Empty,
                            true,
                            true,
                            true,
                            true,
                            true,
                            false,
                            true, true, disksList);
        }

        return result;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
    }

    private boolean canDoSnapshot(VM vm) {
        // if live snapshot is not available, then if vm is up - snapshot is not possible so it needs to be
        // checked if vm up or not
        // if live snapshot is enabled, there is no need to check if vm is up since in any case snapshot is possible
        if (!isLiveSnapshotEnabled() && !vm.isDown()) {
            // if there is no live snapshot and the vm is up - snapshot is not possible
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DATA_CENTER_VERSION_DOESNT_SUPPORT_LIVE_SNAPSHOT);
        }
        return true;
    }

    /**
     * @return If DC level does not support live snapshots.
     */
    private boolean isLiveSnapshotEnabled() {
        return Config.<Boolean> GetValue(
                ConfigValues.LiveSnapshotEnabled, getStoragePool().getcompatibility_version().getValue());
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.CreateSnapshot;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return getParameters().isNeedsLocking() ?
                Collections.singletonMap(getVmId().toString(), LockingGroup.VM.name()) : null;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        for (DiskImage disk : getDisksList()) {
            list.add(new QuotaStorageConsumptionParameter(
                    disk.getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    disk.getstorage_ids().get(0),
                    disk.getActualSize()));
        }

        return list;
    }
}
