package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.RestoreFromSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.vdscommands.DestroyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This command responcible to make snapshot of some Vm mapped to some drive be
 * active snapshot. All children snapshots and other snapshot mapped to same
 * drive will be removed.
 */
@InternalCommandAttribute
public class RestoreFromSnapshotCommand<T extends RestoreFromSnapshotParameters> extends BaseImagesCommand<T> {

    private final java.util.ArrayList<Guid> _imagesToDelete = new java.util.ArrayList<Guid>();

    public RestoreFromSnapshotCommand(T parameters) {
        super(parameters);
        setImageContainerId(getParameters().getContainerId());
    }

    @Override
    protected void executeCommand() {

        super.executeCommand();
        if (RemoveImages()) {
            if (getParameters().getSnapshot().getType() != SnapshotType.REGULAR) {
                getImage().setactive(true);
                DbFacade.getInstance().getDiskImageDAO().update(getImage());
            }

            setSucceeded(true);
        }
    }

    private boolean RemoveImages() {
        Guid imageToRemoveId = findImageForSameDrive(getParameters().getRemovedSnapshotId());

        switch (getParameters().getSnapshot().getType()) {
        case REGULAR:
            RemoveOtherImageAndParents(imageToRemoveId, getDiskImage().getParentId());
            break;
        case PREVIEW:
        case STATELESS:
            if (imageToRemoveId != null) {
                RemoveSnapshot(DbFacade.getInstance().getDiskImageDAO().get(imageToRemoveId));
            }

            break;
        }

        VDSReturnValue vdsReturnValue = performImageVdsmOperation();
        return vdsReturnValue != null && vdsReturnValue.getSucceeded();
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        VdcActionParametersBase commandParams = getParametersForTask(parentCommand, getParameters());
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                commandParams, asyncTaskCreationInfo.getStepId()));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.deleteVolume, p, false);

        return ret;
    }

    @Override
    protected void AdditionalImageRemoveTreatment(DiskImage snapshot) {
        // Vitaly

        _imagesToDelete.add(_imagesToDelete.size(), snapshot.getId());
    }

    private void RemoveOtherImageAndParents(Guid imageId, Guid lastParent) {
        DiskImage image = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(imageId);
        // store other mapped image's parent Id
        Guid currentParent = image.getParentId();
        // Remove other mapped image from Irs and db
        /**
         * Vitaly //_imagesToDelete.Add(image.image_guid);
         */
        RemoveSnapshot(image);
        while (!lastParent.equals(currentParent)) {
            image = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(currentParent);
            // store current image's parent Id
            currentParent = image.getParentId();
            /**
             * Vitaly
             * //_imagesToDelete.Insert(_imagesToDelete.Count,image.image_guid);
             */
            RemoveSnapshot(image);
        }
    }

    @Override
    protected VDSReturnValue performImageVdsmOperation() {
        VDSReturnValue vdsReturnValue = null;
        try {
            Guid storagePoolId = getDiskImage().getstorage_pool_id() != null ? getDiskImage().getstorage_pool_id()
                    .getValue() : Guid.Empty;
            Guid storageDomainId =
                    getDiskImage().getstorage_ids() != null && !getDiskImage().getstorage_ids().isEmpty() ? getDiskImage().getstorage_ids()
                            .get(0)
                            : Guid.Empty;
            Guid imageGroupId = getDiskImage().getimage_group_id() != null ? getDiskImage().getimage_group_id()
                    .getValue() : Guid.Empty;

            vdsReturnValue = runVdsCommand(
                            VDSCommandType.DestroyImage,
                            new DestroyImageVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    _imagesToDelete, getDiskImage().getwipe_after_delete(), true, getStoragePool()
                                            .getcompatibility_version().toString()));

            if (vdsReturnValue.getSucceeded()) {
                getReturnValue().getInternalTaskIdList().add(
                        CreateTask(vdsReturnValue.getCreationInfo(), VdcActionType.RestoreAllSnapshots));
            }
        }
        // Don't throw an exception when cannot destroy image in the VDSM.
        catch (VdcBLLException e) {
            // Set fault for parent command RestoreAllSnapshotCommand to use, if decided to fail the command.
            getReturnValue().setFault(new VdcFault(e, e.getVdsError().getCode()));
            log.info(String.format("%1$s Image not exist in Irs", getDiskImage().getId()));
        }
        return vdsReturnValue;
    }
}
