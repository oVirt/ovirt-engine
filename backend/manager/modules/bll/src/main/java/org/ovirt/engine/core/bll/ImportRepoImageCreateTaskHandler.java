package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class ImportRepoImageCreateTaskHandler implements SPMAsyncTaskHandler {

    private final CommandBase<? extends ImportRepoImageParameters> enclosingCommand;

    private StorageDomain destinationStorageDomain;

    public ImportRepoImageCreateTaskHandler(CommandBase<? extends ImportRepoImageParameters> enclosingCommand) {
        this.enclosingCommand = enclosingCommand;
    }

    @Override
    public void execute() {
        if (enclosingCommand.getParameters().getTaskGroupSuccess()) {
            enclosingCommand.getParameters().setImageGroupID(Guid.newGuid());
            enclosingCommand.getParameters().setDestinationImageId(Guid.newGuid());
            enclosingCommand.getParameters().setEntityInfo(
                    new EntityInfo(VdcObjectType.Disk, enclosingCommand.getParameters().getImageGroupID()));

            // Filling in all the remaining fields of the target DiskImage
            DiskImage diskImage = enclosingCommand.getParameters().getDiskImage();
            ArrayList<Guid> storageIds = new ArrayList<>();
            storageIds.add(enclosingCommand.getParameters().getStorageDomainId());
            diskImage.setStorageIds(storageIds);
            diskImage.setStoragePoolId(enclosingCommand.getParameters().getStoragePoolId());
            diskImage.setId(enclosingCommand.getParameters().getImageGroupID());
            diskImage.setDiskInterface(DiskInterface.VirtIO);

            if (diskImage.getVolumeFormat() == VolumeFormat.RAW &&
                    getDestinationStorageDomain().getStorageType().isBlockDomain()) {
                diskImage.setVolumeType(VolumeType.Preallocated);
            } else {
                diskImage.setVolumeType(VolumeType.Sparse);
            }

            VdcReturnValueBase vdcReturnValue =
                    Backend.getInstance().runInternalAction(VdcActionType.AddImageFromScratch,
                            getAddImageFromScratchParameters(),
                            ExecutionHandler.createDefaultContextForTasks(enclosingCommand.getContext()));

            enclosingCommand.getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());

            if (vdcReturnValue.getActionReturnValue() != null) {
                DiskImage newDiskImage = (DiskImage) vdcReturnValue.getActionReturnValue();
                enclosingCommand.getParameters().setDestinationImageId(newDiskImage.getImageId());
                enclosingCommand.getParameters().getDiskImage().setImageId(newDiskImage.getImageId());
                MultiLevelAdministrationHandler.addPermission(new Permissions(
                        enclosingCommand.getCurrentUser().getId(), PredefinedRoles.DISK_OPERATOR.getId(),
                        newDiskImage.getId(), VdcObjectType.Disk));
            }

            ExecutionHandler.setAsyncJob(enclosingCommand.getExecutionContext(), true);
            enclosingCommand.getReturnValue().setSucceeded(true);
        }
    }

    protected StorageDomain getDestinationStorageDomain() {
        if (destinationStorageDomain == null) {
            destinationStorageDomain = DbFacade.getInstance().getStorageDomainDao().get(
                    enclosingCommand.getParameters().getStorageDomainId());
        }
        return destinationStorageDomain;
    }

    protected AddImageFromScratchParameters getAddImageFromScratchParameters() {
        AddImageFromScratchParameters parameters = new AddImageFromScratchParameters(
                Guid.Empty, null, enclosingCommand.getParameters().getDiskImage());
        parameters.setStoragePoolId(enclosingCommand.getParameters().getStoragePoolId());
        parameters.setStorageDomainId(enclosingCommand.getParameters().getStorageDomainId());
        parameters.setImageGroupID(enclosingCommand.getParameters().getImageGroupID());
        parameters.setQuotaId(enclosingCommand.getParameters().getQuotaId());
        parameters.setDiskProfileId(enclosingCommand.getParameters().getDiskProfileId());
        parameters.setParentCommand(VdcActionType.ImportRepoImage);
        parameters.setParentParameters(enclosingCommand.getParameters());
        parameters.setDestinationImageId(enclosingCommand.getParameters().getDestinationImageId());
        return parameters;
    }

    @Override
    public void endSuccessfully() {
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    @Override
    public void endWithFailure() {
        enclosingCommand.getParameters().getDiskImage().setImageStatus(ImageStatus.ILLEGAL);
        ImagesHandler.updateImageStatus(
                enclosingCommand.getParameters().getDiskImage().getImageId(),
                enclosingCommand.getParameters().getDiskImage().getImageStatus());
        compensate();
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    @Override
    public void compensate() {
        VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(VdcActionType.RemoveDisk,
                new RemoveDiskParameters(enclosingCommand.getParameters().getImageGroupID()));
        enclosingCommand.getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
    }

    @Override
    public AsyncTaskType getTaskType() {
        return null; // No implementation - handled by the command
    }

    @Override
    public AsyncTaskType getRevertTaskType() {
        return AsyncTaskType.deleteImage;
    }

}
