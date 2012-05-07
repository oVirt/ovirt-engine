package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
public class AddImageFromScratchCommand<T extends AddImageFromScratchParameters> extends CreateSnapshotCommand<T> {

    public AddImageFromScratchCommand(T parameters) {
        super(parameters);
        setVmId(getParameters().getMasterVmId());
    }

    @Override
    protected void executeCommand() {
        if (getVm() != null) {
            setImageContainerId(getVm().getvmt_guid());
        }
        setImageGroupId(getParameters().getDiskInfo().getId());

        if (ProcessImageInIrs()) {
            mNewCreatedDiskImage = new DiskImage();
            mNewCreatedDiskImage.setImageId(getDestinationImageId());
            mNewCreatedDiskImage.setInternalDriveMapping(getParameters().getDiskInfo().getInternalDriveMapping());
            mNewCreatedDiskImage.setBoot(getParameters().getDiskInfo().isBoot());
            mNewCreatedDiskImage.setDiskInterface(getParameters().getDiskInfo().getDiskInterface());
            mNewCreatedDiskImage.setPropagateErrors(getParameters().getDiskInfo().getPropagateErrors());
            mNewCreatedDiskImage.setWipeAfterDelete(getParameters().getDiskInfo().isWipeAfterDelete());
            mNewCreatedDiskImage.setDiskAlias(getParameters().getDiskInfo().getDiskAlias());
            mNewCreatedDiskImage.setDiskDescription(getParameters().getDiskInfo().getDiskDescription());
            mNewCreatedDiskImage.setShareable(getParameters().getDiskInfo().isShareable());
            mNewCreatedDiskImage.setAllowSnapshot(getParameters().getDiskInfo().isAllowSnapshot());
            mNewCreatedDiskImage.setvm_guid(getParameters().getMasterVmId());
            mNewCreatedDiskImage.setId(getImageGroupId());
            mNewCreatedDiskImage.setstorage_pool_id(getParameters().getStoragePoolId());
            mNewCreatedDiskImage.setstorage_ids(new ArrayList<Guid>(Arrays.asList(getParameters().getStorageDomainId())));
            mNewCreatedDiskImage.setsize(getParameters().getDiskInfo().getsize());
            mNewCreatedDiskImage.setvolume_type(getParameters().getDiskInfo().getvolume_type());
            mNewCreatedDiskImage.setvolume_format(getParameters().getDiskInfo().getvolume_format());
            mNewCreatedDiskImage.setdescription("");
            mNewCreatedDiskImage.setcreation_date(new Date());
            mNewCreatedDiskImage.setlastModified(new Date());
            mNewCreatedDiskImage.setactive(true);
            mNewCreatedDiskImage.setimageStatus(ImageStatus.LOCKED);
            mNewCreatedDiskImage.setvm_snapshot_id(getParameters().getVmSnapshotId());
            mNewCreatedDiskImage.setQuotaId(getParameters().getQuotaId());

            AddDiskImageToDb(mNewCreatedDiskImage);
            getReturnValue().setActionReturnValue(mNewCreatedDiskImage);
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
                        new CreateImageVDSCommandParameters(getParameters().getStoragePoolId(), getParameters()
                                .getStorageDomainId(), getImageGroupId(), getParameters().getDiskInfo().getsize(),
                                getParameters().getDiskInfo().getvolume_type(), getParameters().getDiskInfo()
                                        .getvolume_format(), getDestinationImageId(), "", getStoragePool()
                                        .getcompatibility_version().toString()));
        if (vdsReturnValue.getSucceeded()) {
            getParameters().setTaskIds(new ArrayList<Guid>());
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
                parametersForTask, asyncTaskCreationInfo.getStepId(), getCommandId()));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.createVolume, p, false);

        return ret;
    }

    @Override
    protected DiskImage getImage() {
        return null;
    }

    @Override
    protected void EndWithFailure() {
        if (getDestinationDiskImage() != null) {
            DbFacade.getInstance().getDiskImageDynamicDAO().remove(getDestinationDiskImage().getImageId());
        }
        super.EndWithFailure();
    }
}
