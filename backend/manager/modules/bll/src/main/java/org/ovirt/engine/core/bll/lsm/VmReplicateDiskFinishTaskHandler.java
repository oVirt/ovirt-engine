package org.ovirt.engine.core.bll.lsm;

import org.ovirt.engine.core.bll.AbstractSPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.ImageStorageDomainMapId;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmReplicateDiskParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.DiskImageDynamicDAO;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class VmReplicateDiskFinishTaskHandler extends AbstractSPMAsyncTaskHandler<TaskHandlerCommand<? extends LiveMigrateDiskParameters>> {
    public VmReplicateDiskFinishTaskHandler(TaskHandlerCommand<? extends LiveMigrateDiskParameters> cmd) {
        super(cmd);
    }

    @Override
    protected void beforeTask() {
        // Split the image
        VmReplicateDiskParameters migrationCompleteParams = new VmReplicateDiskParameters
                (getEnclosingCommand().getParameters().getVdsId(),
                        getEnclosingCommand().getParameters().getVmId(),
                        getEnclosingCommand().getParameters().getStoragePoolId().getValue(),
                        getEnclosingCommand().getParameters().getSourceStorageDomainId(),
                        getEnclosingCommand().getParameters().getTargetStorageDomainId(),
                        getEnclosingCommand().getParameters().getImageGroupID(),
                        getEnclosingCommand().getParameters().getDestinationImageId()
                );

        VDSReturnValue ret =
                Backend.getInstance().getResourceManager().
                        RunVdsCommand(VDSCommandType.VmReplicateDiskFinish, migrationCompleteParams);

        // If the split succeeded, update the database
        if (ret.getSucceeded()) {
            moveDiskInDB();
            updateImagesInfo();
            ImagesHandler.updateImageStatus(getEnclosingCommand().getParameters().getDestinationImageId(),
                    ImageStatus.OK);
        }
        else {
            log.errorFormat("Failed VmReplicateDiskFinish (Disk {0} , VM {1})",
                    getEnclosingCommand().getParameters().getImageGroupID(),
                    getEnclosingCommand().getParameters().getVmId());
            throw new VdcBLLException(ret.getVdsError().getCode(), ret.getVdsError().getMessage());
        }
    }

    private void moveDiskInDB() {
        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                new TransactionMethod<Object>() {
                    @SuppressWarnings("synthetic-access")
                    @Override
                    public Object runInTransaction() {
                        for (DiskImage di : getDiskImageDao().getAllSnapshotsForImageGroup
                                (getEnclosingCommand().getParameters().getImageGroupID())) {
                            getImageStorageDomainMapDao().remove
                                    (new ImageStorageDomainMapId(di.getImageId(),
                                            getEnclosingCommand().getParameters().getSourceStorageDomainId()));
                            getImageStorageDomainMapDao().save
                                    (new image_storage_domain_map(di.getImageId(),
                                            getEnclosingCommand().getParameters().getTargetStorageDomainId()));
                        }
                        return null;
                    }
                });
    }

    private void updateImagesInfo() {
        for (DiskImage image : getDiskImageDao().getAllSnapshotsForImageGroup
                (getEnclosingCommand().getParameters().getImageGroupID())) {
            VDSReturnValue ret = Backend.getInstance().getResourceManager().RunVdsCommand(
                    VDSCommandType.GetImageInfo,
                    new GetImageInfoVDSCommandParameters(getEnclosingCommand().getParameters().getStoragePoolId(),
                            getEnclosingCommand().getParameters().getTargetStorageDomainId(),
                            getEnclosingCommand().getParameters().getImageGroupID(),
                            image.getImageId()));

            DiskImage imageFromIRS = (DiskImage) ret.getReturnValue();
            DiskImageDynamic diskImageDynamic = getDiskImageDynamicDao().get(image.getImageId());

            // Update image's actual size in DB
            if (imageFromIRS != null && diskImageDynamic != null) {
                diskImageDynamic.setactual_size(imageFromIRS.getActualSizeFromDiskImage());
                getDiskImageDynamicDao().update(diskImageDynamic);
            }
        }
    }

    private static DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDao();
    }

    private static ImageStorageDomainMapDao getImageStorageDomainMapDao() {
        return DbFacade.getInstance().getImageStorageDomainMapDao();
    }

    private static DiskImageDynamicDAO getDiskImageDynamicDao() {
        return DbFacade.getInstance().getDiskImageDynamicDao();
    }

    @Override
    protected VDSCommandType getVDSCommandType() {
        return VDSCommandType.DeleteImageGroup;
    }

    @Override
    protected VDSParametersBase getVDSParameters() {
        return new DeleteImageGroupVDSCommandParameters(
                getEnclosingCommand().getParameters().getStoragePoolId().getValue(),
                getEnclosingCommand().getParameters().getSourceStorageDomainId(),
                getEnclosingCommand().getParameters().getImageGroupID(),
                DbFacade.getInstance()
                        .getDiskImageDao()
                        .get(getEnclosingCommand().getParameters().getDestinationImageId())
                        .isWipeAfterDelete(),
                getEnclosingCommand().getParameters().getForceDelete(),
                null);
    }

    @Override
    public void endWithFailure() {
        super.endWithFailure();
    }

    @Override
    protected VdcObjectType getTaskObjectType() {
        return VdcObjectType.VM;
    }

    @Override
    protected Guid[] getTaskObjects() {
        return new Guid[] { getEnclosingCommand().getParameters().getVmId() };
    }

    @Override
    public AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteImage;
    }

    @Override
    protected void revertTask() {
        // Preventing rollback on VmReplicateDiskFinish success
        // (checks whether the disk moved successfully to the target storage domain)
        Guid targetStorageDomainId = getEnclosingCommand().getParameters().getTargetStorageDomainId();
        Guid destinationImageId = getEnclosingCommand().getParameters().getDestinationImageId();
        DiskImage diskImage = getDiskImageDao().get(destinationImageId);
        if (diskImage != null && targetStorageDomainId.equals(diskImage.getStorageIds().get(0))) {
            getEnclosingCommand().getParameters().setExecutionIndex(0);
        }
    }

    @Override
    protected VDSCommandType getRevertVDSCommandType() {
        // No revert task - reverting is handled in the previous handler
        return null;
    }

    @Override
    protected VDSParametersBase getRevertVDSParameters() {
        // No revert task - reverting is handled in the previous handler
        return null;
    }

    @Override
    public AsyncTaskType getRevertTaskType() {
        // No revert task - reverting is handled in the previous handler
        return null;
    }

}
