package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.common.VdcObjectType;
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
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation=true)
public class AddImageFromScratchCommand<T extends AddImageFromScratchParameters> extends CreateSnapshotCommand<T> {

    public AddImageFromScratchCommand(T parameters) {
        super(parameters);
        setVmId(getParameters().getMasterVmId());
    }

    @Override
    protected void executeCommand() {
        setImageGroupId(getParameters().getDiskInfo().getId());
        setDestinationImageId(Guid.NewGuid());

        mNewCreatedDiskImage = new DiskImage();
        mNewCreatedDiskImage.setImageId(getDestinationImageId());
        mNewCreatedDiskImage.setBoot(getParameters().getDiskInfo().isBoot());
        mNewCreatedDiskImage.setDiskInterface(getParameters().getDiskInfo().getDiskInterface());
        mNewCreatedDiskImage.setPropagateErrors(getParameters().getDiskInfo().getPropagateErrors());
        mNewCreatedDiskImage.setWipeAfterDelete(getParameters().getDiskInfo().isWipeAfterDelete());
        mNewCreatedDiskImage.setDiskAlias(getParameters().getDiskInfo().getDiskAlias());
        mNewCreatedDiskImage.setDiskDescription(getParameters().getDiskInfo().getDiskDescription());
        mNewCreatedDiskImage.setShareable(getParameters().getDiskInfo().isShareable());
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

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                AddDiskImageToDb(mNewCreatedDiskImage, getCompensationContext());
                return null;
            }
        });
        freeLock();
        ProcessImageInIrs();
        getReturnValue().setActionReturnValue(mNewCreatedDiskImage);
        setSucceeded(true);
    }

    protected boolean ProcessImageInIrs() {
        VDSReturnValue vdsReturnValue = runVdsCommand(
                        VDSCommandType.CreateImage,
                        new CreateImageVDSCommandParameters(getParameters().getStoragePoolId(), getParameters()
                                .getStorageDomainId(), getImageGroupId(), getParameters().getDiskInfo().getsize(),
                                getParameters().getDiskInfo().getvolume_type(), getParameters().getDiskInfo()
                                        .getvolume_format(), getDestinationImageId(), "", getStoragePool()
                                        .getcompatibility_version().toString()));
        if (vdsReturnValue.getSucceeded()) {
            getParameters().setTaskIds(new ArrayList<Guid>());
            getParameters().getTaskIds().add(
                    CreateTask(vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));
            getReturnValue().getInternalTaskIdList().add(getParameters().getTaskIds().get(0));

            return true;
        }

        return false;
    }

    @Override
    protected SPMAsyncTask ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        VdcActionParametersBase parametersForTask = getParametersForTask(parentCommand, getParameters());
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                parametersForTask, asyncTaskCreationInfo.getStepId(), getCommandId()));
        p.setEntityId(getParameters().getEntityId());
        return AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.createVolume, p);
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
