package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.RestoreFromSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * Restores the given snapshot, including all the VM configuration that was stored in it.<br>
 * Any obsolete snapshots will be deleted:<br>
 * * If the restore is done to the {@link SnapshotType#STATELESS} snapshot then the stateless snapshot data is restored
 * into the active snapshot, and the "old" active snapshot is deleted & replaced by the stateless snapshot.<br>
 * * If the restore is done to a branch of a snapshot which is {@link SnapshotStatus#IN_PREVIEW}, then the other branch
 * will be deleted (ie if the {@link SnapshotType#ACTIVE} snapshot is kept, then the branch of
 * {@link SnapshotType#PREVIEW} is deleted up to the previewed snapshot, otherwise the active one is deleted).<br>
 * <br>
 * <b>Note:</b> It is <b>NOT POSSIBLE</b> to restore to a snapshot of any other type other than those stated above,
 * since this command can only handle the aforementioned cases.
 */
@LockIdNameAttribute
public class RestoreAllSnapshotsCommand<T extends RestoreAllSnapshotsParameters> extends VmCommand<T> implements QuotaStorageDependent {

    private static final long serialVersionUID = -461387501474222174L;

    private final List<Guid> snapshotsToRemove = new ArrayList<Guid>();

    /**
     * The snapshot being restored to.
     */
    private Snapshot targetSnapshot;

    /**
     * The snapshot id which will be removed (the stateless/preview/active image).
     */
    private Guid removedSnapshotId;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RestoreAllSnapshotsCommand(Guid commandId) {
        super(commandId);
    }

    public RestoreAllSnapshotsCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());
    }

    @Override
    protected void executeVmCommand() {

        if (!getImagesList().isEmpty()) {
            lockVmWithCompensationIfNeeded();
            if (!isInternalExecution()) {
                freeLock();
            }
        }

        restoreSnapshotAndRemoveObsoleteSnapshots();

        boolean succeeded = true;
        for (DiskImage image : getImagesList()) {
            if (image.getimageStatus() != ImageStatus.ILLEGAL) {
                ImagesContainterParametersBase params = new RestoreFromSnapshotParameters(image.getImageId(),
                        getVmId(), targetSnapshot, removedSnapshotId);
                VdcReturnValueBase returnValue = runAsyncTask(VdcActionType.RestoreFromSnapshot, params);
                // Save the first fault
                if (succeeded && !returnValue.getSucceeded()) {
                    succeeded = false;
                    getReturnValue().setFault(returnValue.getFault());
                }
            }
        }

        removeSnapshotsFromDB();

        if (!getTaskIdList().isEmpty()) {
            deleteOrphanedImages();
        } else {
            getVmStaticDAO().incrementDbGeneration(getVm().getId());
            unlockVm();
        }

        setSucceeded(succeeded);
    }

    protected void removeSnapshotsFromDB() {
        for (Guid snapshotId : snapshotsToRemove) {
            getSnapshotDao().remove(snapshotId);
        }
    }

    protected void deleteOrphanedImages() {
        VdcReturnValueBase returnValue;
        boolean noImagesRemovedYet = getTaskIdList().isEmpty();
        List<Guid> deletedDisksIds = new ArrayList<Guid>();
        for (DiskImage image : getDiskImageDao().getImagesWithNoDisk(getVm().getId())) {
            if (!deletedDisksIds.contains(image.getId())) {
                deletedDisksIds.add(image.getId());
                returnValue = runAsyncTask(VdcActionType.RemoveImage,
                        new RemoveImageParameters(image.getImageId()));
                if (!returnValue.getSucceeded() && noImagesRemovedYet) {
                    setSucceeded(false);
                    getReturnValue().setFault(returnValue.getFault());
                    return;
                }

                noImagesRemovedYet = false;
            }
        }
    }

    /**
     * Run the given command as async task, which includes these steps:
     * <ul>
     * <li>Add parent info to task parameters.</li>
     * <li>Run with current command's {@link org.ovirt.engine.core.bll.job.ExecutionContext}.</li>
     * <li>Add son parameters to saved image parameters.</li>
     * <li>Add son task IDs to list of task IDs.</li>
     * </ul>
     *
     * @param taskType
     *            The type of the command to run as async task.
     * @param params
     *            The command parameters.
     * @return The return value from the task.
     */
    private VdcReturnValueBase runAsyncTask(VdcActionType taskType, ImagesContainterParametersBase params) {
        VdcReturnValueBase returnValue;
        params.setEntityId(getParameters().getEntityId());
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setCommandType(taskType);
        returnValue = Backend.getInstance().runInternalAction(
                taskType,
                params,
                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        getParameters().getImagesParameters().add(params);
        getTaskIdList().addAll(returnValue.getInternalTaskIdList());
        return returnValue;
    }

    /**
     * Restore the snapshot - if it is not the active snapshot, then the VM configuration will be restored.<br>
     * Additionally, remove all obsolete snapshots (The one after stateless, or the preview chain which was not chosen).
     */
    protected void restoreSnapshotAndRemoveObsoleteSnapshots() {
        targetSnapshot = getSnapshotDao().get(getParameters().getDstSnapshotId());

        if (targetSnapshot == null) {
            throw new VdcBLLException(VdcBllErrors.ENGINE, "Can't find target snapshot by id: "
                    + getParameters().getDstSnapshotId());
        }

        switch (targetSnapshot.getType()) {
        case PREVIEW:
            getSnapshotDao().updateStatus(
                    getSnapshotDao().getId(getVmId(), SnapshotType.REGULAR, SnapshotStatus.IN_PREVIEW),
                    SnapshotStatus.OK);
        case STATELESS:
            restoreConfiguration(targetSnapshot);
            break;

        // Currently UI sends the "in preview" snapshot to restore when "Commit" is pressed.
        case REGULAR:
            if (SnapshotStatus.IN_PREVIEW == targetSnapshot.getStatus()) {
                prepareToDeletePreviewBranch();

                // Set the active snapshot's images as target images for restore, because they are what we keep.
                getParameters().setImagesList(getDiskImageDao().getAllSnapshotsForVmSnapshot(
                        getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE)));
                break;
            }
        default:
            throw new VdcBLLException(VdcBllErrors.ENGINE, "No support for restoring to snapshot type: "
                    + targetSnapshot.getType());
        }
    }

    /**
     * Prepare to remove the active snapshot & restore the given snapshot to be the active one, including the
     * configuration.
     *
     * @param targetSnapshot
     *            The snapshot to restore to.
     */
    private void restoreConfiguration(Snapshot targetSnapshot) {
        SnapshotsManager snapshotsManager = new SnapshotsManager();
        removedSnapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
        snapshotsToRemove.add(removedSnapshotId);
        snapshotsManager.removeAllIllegalDisks(removedSnapshotId, getVmId());

        snapshotsManager.attempToRestoreVmConfigurationFromSnapshot(getVm(),
                targetSnapshot,
                targetSnapshot.getId(),
                getCompensationContext());
        getSnapshotDao().remove(targetSnapshot.getId());
        snapshotsManager.addActiveSnapshot(targetSnapshot.getId(), getVm(), getCompensationContext());
    }

    /**
     * All snapshots who derive from the snapshot which is {@link SnapshotStatus#IN_PREVIEW}, up to it (excluding), will
     * be queued for deletion.<br>
     * The traversal between snapshots is done according to the {@link DiskImage} level.
     */
    protected void prepareToDeletePreviewBranch() {
        removedSnapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.PREVIEW);
        Guid previewedSnapshotId =
                getSnapshotDao().getId(getVmId(), SnapshotType.REGULAR, SnapshotStatus.IN_PREVIEW);
        getSnapshotDao().updateStatus(previewedSnapshotId, SnapshotStatus.OK);
        snapshotsToRemove.add(removedSnapshotId);
        List<DiskImage> images = getDiskImageDao().getAllSnapshotsForVmSnapshot(removedSnapshotId);

        for (DiskImage image : images) {
            DiskImage parentImage = getDiskImageDao().getSnapshotById(image.getParentId());
            NGuid snapshotToRemove = (parentImage == null) ? null : parentImage.getvm_snapshot_id();

            while (parentImage != null && snapshotToRemove != null && !snapshotToRemove.equals(previewedSnapshotId)) {
                if (!snapshotsToRemove.contains(snapshotToRemove.getValue())) {
                    snapshotsToRemove.add(snapshotToRemove.getValue());
                }

                parentImage = getDiskImageDao().getSnapshotById(parentImage.getParentId());
                snapshotToRemove = (parentImage == null) ? null : parentImage.getvm_snapshot_id();
            }
        }
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.RestoreFromSnapshot;
    }

    private List<DiskImage> getImagesList() {
        if (getParameters().getImagesList() == null && !getParameters().getDstSnapshotId().equals(Guid.Empty)) {
            getParameters().setImagesList(
                    getDiskImageDao().getAllSnapshotsForVmSnapshot(getParameters().getDstSnapshotId()));
        }
        return getParameters().getImagesList();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_RESTORE_FROM_SNAPSHOT_START
                    : AuditLogType.USER_FAILED_RESTORE_FROM_SNAPSHOT;
        default:
            return AuditLogType.USER_RESTORE_FROM_SNAPSHOT_FINISH_SUCCESS;
        }
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            Snapshot snapshot = getSnapshotDao().get(getParameters().getDstSnapshotId());
            if (snapshot != null) {
                jobProperties.put(VdcObjectType.Snapshot.name().toLowerCase(), snapshot.getDescription());
            }
        }
        return jobProperties;
    }

    @Override
    protected boolean canDoAction() {
        boolean result = false;
        if (!getSnapshotDao().exists(getVmId(), getParameters().getDstSnapshotId())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
        } else {
            result = performImagesChecks();
            if (result && (getVm().getStatus() != VMStatus.Down)) {
                result = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            }
        }

        if (result) {
            Snapshot snapshot = getSnapshotDao().get(getParameters().getDstSnapshotId());
            if (snapshot.getType() == SnapshotType.REGULAR
                    && snapshot.getStatus() != SnapshotStatus.IN_PREVIEW) {
                result = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_NOT_IN_PREVIEW);
            }
        }

        if (!result) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REVERT_TO);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
        }
        return result;
    }

    protected boolean performImagesChecks() {
        return ImagesHandler.PerformImagesChecks
                (getVm(),
                        getReturnValue().getCanDoActionMessages(),
                        getVm().getStoragePoolId(),
                        Guid.Empty,
                        true,
                        true,
                        false,
                        false,
                        false,
                        false,
                        true,
                        true,
                        getImagesList());
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(), LockingGroup.VM.name());
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        //
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        // TODO: need to be fixed. sp id should be available
        setStoragePoolId(getImagesList().get(0).getstorage_pool_id());

        for (DiskImage image : getImagesList()) {
            if (!image.getImage().isActive() && image.getQuotaId() != null && !Guid.Empty.equals(image.getQuotaId())) {
                list.add(new QuotaStorageConsumptionParameter(image.getQuotaId(), null,
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        image.getstorage_ids().get(0),
                        image.getActualSize()));
            }
        }

        return list;
    }
}
