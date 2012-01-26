package org.ovirt.engine.core.bll;

import java.util.Date;

import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.IImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
public class AddImageFromScratchCommand<T extends AddImageFromScratchParameters> extends CreateSnapshotCommand<T> {
    private DiskImageTemplate mTemplate;

    public AddImageFromScratchCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getMasterVmId());
    }

    @Override
    protected void executeCommand() {
        setImageContainerId(getVm().getvmt_guid());
        setImageGroupId(Guid.NewGuid());

        if (ProcessImageInIrs()) {
            mNewCreatedDiskImage = new DiskImage();
            mNewCreatedDiskImage.setId(getDestinationImageId());
            mNewCreatedDiskImage.setinternal_drive_mapping(getParameters().getDiskInfo().getinternal_drive_mapping());
            mNewCreatedDiskImage.setboot(getParameters().getDiskInfo().getboot());
            mNewCreatedDiskImage.setdisk_interface(getParameters().getDiskInfo().getdisk_interface());
            mNewCreatedDiskImage.setpropagate_errors(getParameters().getDiskInfo().getpropagate_errors());
            mNewCreatedDiskImage.setwipe_after_delete(getParameters().getDiskInfo().getwipe_after_delete());
            mNewCreatedDiskImage.setvm_guid(getVmId());
            mNewCreatedDiskImage.setimage_group_id(getImageGroupId());
            mNewCreatedDiskImage.setstorage_pool_id(getVm().getstorage_pool_id());
            mNewCreatedDiskImage.setstorage_id(getParameters().getStorageDomainId());
            mNewCreatedDiskImage.setsize(getParameters().getDiskInfo().getsize());
            mNewCreatedDiskImage.setvolume_type(getParameters().getDiskInfo().getvolume_type());
            mNewCreatedDiskImage.setvolume_format(getParameters().getDiskInfo().getvolume_format());
            mNewCreatedDiskImage.setdisk_type(getParameters().getDiskInfo().getdisk_type());
            mNewCreatedDiskImage.setdescription(CalculateImageDescription());
            mNewCreatedDiskImage.setcreation_date(new Date());
            mNewCreatedDiskImage.setlastModified(new Date());
            mNewCreatedDiskImage.setactive(true);
            mNewCreatedDiskImage.setimageStatus(ImageStatus.LOCKED);
            mNewCreatedDiskImage.setvm_snapshot_id(getParameters().getVmSnapshotId());

            AddDiskImageToDb(mNewCreatedDiskImage);
            getReturnValue().setActionReturnValue(mNewCreatedDiskImage.getId());
            setSucceeded(true);
        }
    }

    protected boolean ProcessImageInIrs() {
        setDestinationImageId(Guid.NewGuid());

        VDSReturnValue vdsReturnValue = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.CreateImage,
                        new CreateImageVDSCommandParameters(getVm().getstorage_pool_id(), getParameters()
                                .getStorageDomainId(), getImageGroupId(), getParameters().getDiskInfo().getsize(),
                                getParameters().getDiskInfo().getvolume_type(), getParameters().getDiskInfo()
                                        .getvolume_format(), getParameters().getDiskInfo().getdisk_type(),
                                getDestinationImageId(), CalculateImageDescription(), getStoragePool()
                                        .getcompatibility_version().toString()));

        if (vdsReturnValue.getSucceeded()) {
            getParameters().setTaskIds(new java.util.ArrayList<Guid>());
            getParameters().getTaskIds().add(
                    CreateTask(vdsReturnValue.getCreationInfo(), getParameters().getParentCommand()));
            getReturnValue().getInternalTaskIdList().add(getParameters().getTaskIds().get(0));

            return true;
        }

        return false;
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        VdcActionParametersBase parametersForTask = getParametersForTask(parentCommand, getParameters());
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                parametersForTask, asyncTaskCreationInfo.getStepId()));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.createVolume, p, false);

        return ret;
    }

    @Override
    protected IImage getImage() {
        if (mTemplate == null) {
            // use blank id for template because we create image from scratch
            mTemplate =
                    DbFacade.getInstance()
                            .getDiskImageTemplateDAO()
                            .getByVmTemplateAndId(ImagesHandler.BlankImageTemplateId, getImageId());
        }
        return mTemplate;
    }

    @Override
    protected void EndWithFailure() {
        if (getDestinationDiskImage() != null) {
            if (DbFacade.getInstance().getDiskImageDynamicDAO().get(getDestinationDiskImage().getId()) != null) {
                DbFacade.getInstance().getDiskImageDynamicDAO().remove(getDestinationDiskImage().getId());
            }
        }

        RevertTasks();

        super.EndWithFailure();
    }
}
