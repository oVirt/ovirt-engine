package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.businessentities.image_vm_map_id;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.vdscommands.DestroyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This command responcible to make snapshot of some Vm mapped to some drive be
 * active snapshot. All children snapshots and other snapshot mapped to same
 * drive will be removed.
 */
@InternalCommandAttribute
public class RestoreFromSnapshotCommand<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {
    /**
     * patch
     */
    private Guid mImageIdToRestore;
    private Guid mOtherImageId;

    private final java.util.ArrayList<Guid> _imagesToDelete = new java.util.ArrayList<Guid>();

    public RestoreFromSnapshotCommand(T parameters) {
        super(parameters);
        mImageIdToRestore = mImageIdToRestore == null ? Guid.Empty : mImageIdToRestore;
        mOtherImageId = mOtherImageId == null ? Guid.Empty : mOtherImageId;
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        if (RemoveImages()) {
            SaveImageVmMapToDb();
            setSucceeded(true);
        }
    }

    private void RemoveOtherMappedImages() {
        DiskImage image;
        while ((image = GetOtherImageMappedToSameDrive()) != null) {
            DbFacade.getInstance()
                    .getImageVmMapDAO()
                    .remove(new image_vm_map_id(image.getId(), getDiskImage().getvm_guid()));
            /**
             * Vitaly //_imagesToDelete.Insert(_imagesToDelete.Count,
             * image.image_guid);
             */
            RemoveSnapshot(image);
        }
    }

    private boolean RemoveImages() {
        (getParameters()).setImageGroupID(getDiskImage().getimage_group_id().getValue());
        if (!mOtherImageId.equals(Guid.Empty)) {

            // Restoring done to trieng image. - active.
            // all other child images of trieng image's parent should be removed
            // They are: other mapped image and all its parents until trying
            // image parent
            DbFacade.getInstance()
                    .getImageVmMapDAO()
                    .remove(new image_vm_map_id(mOtherImageId, getDiskImage().getvm_guid()));
            RemoveOtherImageAndParents(mOtherImageId, getDiskImage().getParentId());
        } else {
            if (getDiskImage().getactive() != null && getDiskImage().getactive().equals(false)) {
                RemoveOtherMappedImages();
            }
            RemoveChildren(getImage().getId());
        }
        return RemoveImagesInIrs();
    }

    /**
     * This function encapsulated processing of removing image in Irs. If image
     * not exists - no exception re throwing. This functionality aids to remove
     * Vm/Vmtemplate even if one or more its images not exists in Irs.
     */
    private boolean RemoveImagesInIrs() {
        try {
            Guid storagePoolId = getDiskImage().getstorage_pool_id() != null ? getDiskImage().getstorage_pool_id()
                    .getValue() : Guid.Empty;
            Guid storageDomainId = getDiskImage().getstorage_id() != null ? getDiskImage().getstorage_id().getValue()
                    : Guid.Empty;
            Guid imageGroupId = getDiskImage().getimage_group_id() != null ? getDiskImage().getimage_group_id()
                    .getValue() : Guid.Empty;

            VDSReturnValue vdsReturnValue = runVdsCommand(
                            VDSCommandType.DestroyImage,
                            new DestroyImageVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    _imagesToDelete, getDiskImage().getwipe_after_delete(), true, getStoragePool()
                                            .getcompatibility_version().toString()));

            if (vdsReturnValue.getSucceeded()) {
                getReturnValue().getInternalTaskIdList().add(
                        CreateTask(vdsReturnValue.getCreationInfo(), VdcActionType.RestoreAllSnapshots));
            } else {
                return false;
            }
        }
        // Don't throw an exception when cannot destroy image in the VDSM.
        catch (VdcBLLException e) {
            // Set fault for parent command RestoreAllSnapshotCommand to use, if decided to fail the command.
            getReturnValue().setFault(new VdcFault(e, e.getVdsError().getCode()));
            log.info(String.format("%1$s Image not exist in Irs", getDiskImage().getId()));
        }

        return true;
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        VdcActionParametersBase commandParams = getParametersForTask(parentCommand, getParameters());
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                commandParams));
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

    private void SaveImageVmMapToDb() {
        DiskImage image = DbFacade.getInstance().getDiskImageDAO().get(getImage().getId());
        if (image != null) {
            // //Restoring inactive image
            DbFacade.getInstance().getImageVmMapDAO().update(
                    new image_vm_map(true, image.getId(), image.getcontainer_guid()));
        } else {
            DbFacade.getInstance().getImageVmMapDAO().save(
                    new image_vm_map(true, getImage().getId(), getImage().getcontainer_guid()));
        }
    }

    /**
     * During trying image new snapshot created to image, user wish to try. If
     * user wish to work with image, he tried - he will work with snapshot
     * instead of the original image. In this case ImageId will be not the
     * original image Id, But the snapshots. We assuming that this case can
     * occure only during Trying image. So there are two images map to same
     * drive will appiare in db and active is the one - user will work with.
     * Inactive will be removed
     */
    @Override
    protected Guid getImageId() {
        if (mImageIdToRestore == null || mImageIdToRestore.equals(Guid.Empty)) {
            mImageIdToRestore = super.getImageId();
            DiskImage activeImage = null;
            DiskImage inactiveImage = null;
            RefObject<DiskImage> tempRefObject = new RefObject<DiskImage>(activeImage);
            RefObject<DiskImage> tempRefObject2 = new RefObject<DiskImage>(inactiveImage);
            int count = ImagesHandler.getImagesMappedToDrive(getImageContainerId(), getDrive(), tempRefObject,
                    tempRefObject2);
            activeImage = tempRefObject.argvalue;
            inactiveImage = tempRefObject2.argvalue;
            if (count == 2) {
                if (activeImage != null && inactiveImage != null) {
                    if (super.getImageId().equals(activeImage.getParentId())) {
                        mImageIdToRestore = activeImage.getId();
                        mOtherImageId = inactiveImage.getId();
                    }
                }
            }
        }
        return mImageIdToRestore;
    }

    private static LogCompat log = LogFactoryCompat.getLog(RestoreFromSnapshotCommand.class);
}
