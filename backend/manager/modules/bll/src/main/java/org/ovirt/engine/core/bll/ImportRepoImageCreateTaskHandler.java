package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class ImportRepoImageCreateTaskHandler implements SPMAsyncTaskHandler {

    private final TaskHandlerCommand<? extends ImportRepoImageParameters> enclosingCommand;

    private StorageDomain destinationStorageDomain;

    public ImportRepoImageCreateTaskHandler(TaskHandlerCommand<? extends ImportRepoImageParameters> enclosingCommand) {
        this.enclosingCommand = enclosingCommand;
    }

    @Override
    public void execute() {
        if (enclosingCommand.getParameters().getTaskGroupSuccess()) {
            enclosingCommand.getParameters().setImageGroupID(Guid.newGuid());
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
                            getAddImageFromScratchParameters());

            enclosingCommand.getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());

            if (vdcReturnValue.getActionReturnValue() != null) {
                DiskImage newDiskImage = (DiskImage) vdcReturnValue.getActionReturnValue();
                enclosingCommand.getParameters().setDestinationImageId(newDiskImage.getImageId());
                enclosingCommand.getParameters().getDiskImage().setImageId(newDiskImage.getImageId());
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
        parameters.setParentCommand(VdcActionType.ImportRepoImage);
        parameters.setParentParameters(enclosingCommand.getParameters());
        return parameters;
    }

    @Override
    public void endSuccessfully() {
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    @Override
    public void endWithFailure() {
        deleteImageGroup();
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    @Override
    public void compensate() {
        deleteImageGroup();
    }

    protected void deleteImageGroup() {
        VDSReturnValue vdsReturnValue =
                Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.DeleteImageGroup,
                    new DeleteImageGroupVDSCommandParameters(
                            enclosingCommand.getParameters().getStoragePoolId(),
                            enclosingCommand.getParameters().getStorageDomainId(),
                            enclosingCommand.getParameters().getImageGroupID(),
                            true, true));

        AsyncTaskCreationInfo taskCreationInfo = vdsReturnValue.getCreationInfo();
        enclosingCommand.getReturnValue().getInternalVdsmTaskIdList().add(enclosingCommand.createTask(
                Guid.Empty,
                taskCreationInfo,
                enclosingCommand.getActionType(),
                VdcObjectType.Disk,
                new Guid[] { enclosingCommand.getParameters().getImageGroupID() })
        );

        Guid vdsmTaskId = taskCreationInfo.getVdsmTaskId();
        enclosingCommand.getReturnValue().getVdsmTaskIdList().add(vdsmTaskId);
    }

    @Override
    public AsyncTaskType getTaskType() {
        return null; // No implementation - handled by the command
    }

    @Override
    public AsyncTaskType getRevertTaskType() {
        return null; // No implementation - handled by the command
    }

}
