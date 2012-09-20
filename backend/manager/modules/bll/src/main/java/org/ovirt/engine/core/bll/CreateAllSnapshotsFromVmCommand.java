package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.Quotable;
import org.ovirt.engine.core.bll.quota.StorageQuotaValidationParameter;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
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
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@LockIdNameAttribute
public class CreateAllSnapshotsFromVmCommand<T extends CreateAllSnapshotsFromVmParameters> extends VmCommand<T> implements Quotable {

    private static final long serialVersionUID = -2407757772735253053L;
    List<DiskImage> selectedActiveDisks;

    protected CreateAllSnapshotsFromVmCommand(Guid commandId) {
        super(commandId);
    }

    public CreateAllSnapshotsFromVmCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());
        setSnapshotName(parameters.getDescription());
        setStoragePoolId(getVm().getstorage_pool_id());
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

    @Override
    protected void executeVmCommand() {
        Guid newActiveSnapshotId = Guid.NewGuid();
        Guid createdSnapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
        getParameters().setSnapshotType(determineSnapshotType());

        getSnapshotDao().updateId(createdSnapshotId, newActiveSnapshotId);
        new SnapshotsManager().addSnapshot(createdSnapshotId,
                getParameters().getDescription(),
                getParameters().getSnapshotType(),
                getVm(),
                getCompensationContext());

        freeLock();
        setActionReturnValue(createdSnapshotId);

        if (getDisksList().isEmpty()) {
            getParameters().setTaskGroupSuccess(true);
            endSuccessfully();
        } else {
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
                parrentParamsForTask.getImagesParameters().add(p);

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

            setSucceeded(true);
        }
    }

    /**
     * @return For internal execution, return the type from parameters, otherwise return {@link SnapshotType#REGULAR}.
     */
    protected SnapshotType determineSnapshotType() {
        return isInternalExecution() ? getParameters().getSnapshotType() : SnapshotType.REGULAR;
    }

    @Override
    protected void endVmCommand() {
        Guid createdSnapshotId =
                getSnapshotDao().getId(getVmId(), getParameters().getSnapshotType(), SnapshotStatus.LOCKED);
        if (getParameters().getTaskGroupSuccess()) {
            getSnapshotDao().updateStatus(createdSnapshotId, SnapshotStatus.OK);

            if (getParameters().getParentCommand() != VdcActionType.RunVm && getVm() != null && getVm().isStatusUp()
                    && getVm().getrun_on_vds() != null) {
                performLiveSnapshot(createdSnapshotId);
            }
        } else {
            revertToActiveSnapshot(createdSnapshotId);
        }

        endActionOnDisks();

        updateVmInSpm(getVm().getstorage_pool_id(), Arrays.asList(new VM[] { getVm() }));

        setSucceeded(getParameters().getTaskGroupSuccess());
        getReturnValue().setEndActionTryAgain(false);
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
                            new SnapshotVDSCommandParameters(getVm().getrun_on_vds().getValue(),
                                    getVm().getId(),
                                    ImagesHandler.filterImageDisks(pluggedDisks, false, true)));
                    return null;
                }
            });
        } catch (VdcBLLException e) {
            if (e.getErrorCode() == VdcBllErrors.SNAPSHOT_FAILED) {
                getParameters().setTaskGroupSuccess(false);
                log.errorFormat("Wasn't able to live snpashot due to error: {0}, rolling back.",
                        ExceptionUtils.getMessage(e));
                revertToActiveSnapshot(createdSnapshotId);
            } else {
                throw e;
            }
        }
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
                    && ImagesHandler.PerformImagesChecks(getVm(),
                            getReturnValue().getCanDoActionMessages(),
                            getVm().getstorage_pool_id(),
                            Guid.Empty,
                            true,
                            true,
                            true,
                            true,
                            true,
                            checkVmIsDown(),
                            true, true, disksList);
        }
        return result;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
    }

    /**
     * @return Check for VM down only if DC level does not support live snapshots.
     */
    private boolean checkVmIsDown() {
        return !Config.<Boolean> GetValue(
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
    public boolean validateAndSetQuota() {
        if (isInternalExecution()) {
            return true;
        }
        return getQuotaManager().validateAndSetStorageQuota(getStoragePool(),
                getStorageQuotaListParameters(),
                getReturnValue().getCanDoActionMessages());
    }

    @Override
    public void rollbackQuota() {
        if (isInternalExecution()) {
            return;
        }
        getQuotaManager().rollbackQuota(getStoragePool(),
                getQuotaManager().getQuotaListFromParameters(getStorageQuotaListParameters()));
    }

    private List<StorageQuotaValidationParameter> getStorageQuotaListParameters() {
        List<StorageQuotaValidationParameter> list = new ArrayList<StorageQuotaValidationParameter>();
        for (DiskImage disk : getDisksList()) {
            list.add(new StorageQuotaValidationParameter(disk.getQuotaId() != null ? disk.getQuotaId()
                    : getVm().getQuotaId(),
                    //TODO: shared disk?
                    disk.getstorage_ids().get(0),
                    disk.getActualSize()));
        }
        return list;
    }

    @Override
    public Guid getQuotaId() {
        return null;
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        // no need to check permissions for snapshots, it is inherited from the disk
    }

}
