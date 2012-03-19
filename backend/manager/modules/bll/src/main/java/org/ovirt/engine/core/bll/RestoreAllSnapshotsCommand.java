package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.AuditLogType;
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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

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
public class RestoreAllSnapshotsCommand<T extends RestoreAllSnapshotsParameters> extends VmCommand<T> {

    private static final long serialVersionUID = -461387501474222174L;

    private List<Guid> snapshotsToRemove = new ArrayList<Guid>();

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
    protected void ExecuteVmCommand() {
        if (getImagesList().size() > 0) {
            lockVmWithCompensationIfNeeded();
            restoreSnapshotAndRemoveObsoleteSnapshots();

            VdcReturnValueBase returnValue = null;
            for (DiskImage image : getImagesList()) {
                if (image.getimageStatus() != ImageStatus.ILLEGAL) {
                    ImagesContainterParametersBase params = new RestoreFromSnapshotParameters(image.getId(),
                            image.getinternal_drive_mapping(), getVmId(), targetSnapshot, removedSnapshotId);
                    returnValue = runAsyncTask(VdcActionType.RestoreFromSnapshot, params);
                }
            }

            // We should have at least one task in the VDSM, to be sure that EndCommand will be called and the VM would
            // change its status from Image lock.
            if (getTaskIdList().size() == 0) {
                log.errorFormat("Can't restore snapshot for VM, since no destroyImage task could be established in the VDSM.");
                if (returnValue != null) {
                    getReturnValue().setFault(returnValue.getFault());
                    return;
                }
            } else {
                setSucceeded(true);
            }

            deleteOrphanedImages();

            for (Guid snapshotId : snapshotsToRemove) {
                getSnapshotDao().remove(snapshotId);
            }
        } else {
            setSucceeded(true);
        }
    }

    protected void deleteOrphanedImages() {
        VdcReturnValueBase returnValue;
        boolean noImagesRemovedYet = getTaskIdList().isEmpty();
        List<Guid> deletedDisksIds = new ArrayList<Guid>();
        for (DiskImage image : getDiskImageDAO().getImagesWithNoDisk(getVm().getId())) {
            if (!deletedDisksIds.contains(image.getimage_group_id())) {
                deletedDisksIds.add(image.getimage_group_id());
                returnValue = runAsyncTask(VdcActionType.RemoveImage,
                        new RemoveImageParameters(image.getId(), getVmId()));
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
        params.setParentParemeters(getParameters());
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

        switch(targetSnapshot.getType()) {
        case PREVIEW:
            getSnapshotDao().updateStatus(getSnapshotDao().getId(getVmId(),
                    SnapshotType.REGULAR,
                    SnapshotStatus.IN_PREVIEW),
                    SnapshotStatus.OK);
        case STATELESS:
            restoreConfiguration(targetSnapshot);
            break;

        // Currently UI sends the "in preview" snapshot to restore when "Commit" is pressed.
        case REGULAR:
            if (SnapshotStatus.IN_PREVIEW == targetSnapshot.getStatus()) {
                prepareToDeletePreviewBranch();

                // Set the active snapshot's images as target images for restore, because they are what we keep.
                getParameters().setImagesList(getDiskImageDAO().getAllSnapshotsForVmSnapshot(
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
        snapshotsManager.removeAllIllegalDisks(removedSnapshotId);

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
        List<DiskImage> images = getDiskImageDAO().getAllSnapshotsForVmSnapshot(removedSnapshotId);

        for (DiskImage image : images) {
            DiskImage parentImage = getDiskImageDAO().getSnapshotById(image.getParentId());
            NGuid snapshotToRemove = (parentImage == null) ? null : parentImage.getvm_snapshot_id();

            while (parentImage != null && snapshotToRemove != null && !snapshotToRemove.equals(previewedSnapshotId)) {
                if (snapshotToRemove != null && !snapshotsToRemove.contains(snapshotToRemove.getValue())) {
                    snapshotsToRemove.add(snapshotToRemove.getValue());
                }

                parentImage = getDiskImageDAO().getSnapshotById(parentImage.getParentId());
                snapshotToRemove = (parentImage == null) ? null : parentImage.getvm_snapshot_id();
            }
        }
    }

    protected DiskImageDAO getDiskImageDAO() {
        return DbFacade.getInstance().getDiskImageDAO();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.RestoreFromSnapshot;
    }

    private List<DiskImage> getImagesList() {
        if (getParameters().getImagesList() == null && !getParameters().getDstSnapshotId().equals(Guid.Empty)) {
            getParameters().setImagesList(
                    getDiskImageDAO().getAllSnapshotsForVmSnapshot(getParameters().getDstSnapshotId()));
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
    protected boolean canDoAction() {
        boolean result = false;
        if (getImagesList() == null || getImagesList().isEmpty()
                || !getSnapshotDao().exists(getVmId(), getParameters().getDstSnapshotId())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
        } else {
            result = ImagesHandler.PerformImagesChecks(getVm(), getReturnValue().getCanDoActionMessages(), getVm()
                    .getstorage_pool_id(), Guid.Empty, true, true, false, false,
                    false, false, true, true, getImagesList());
            if (result && (getVm().getstatus() != VMStatus.Down)) {
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

    private static Log log = LogFactory.getLog(RemoveAllVmImagesCommand.class);
}
