package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.CreateImageTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This command responsible to create new Image Template from image.
 */
@InternalCommandAttribute
public class CreateImageTemplateCommand<T extends CreateImageTemplateParameters> extends BaseImagesCommand<T> {
    public CreateImageTemplateCommand(T parameters) {
        super(parameters);
        super.setVmTemplateId(parameters.getVmTemplateId());
        super.setVmTemplateName(parameters.getVmTemplateName());
    }

    @Override
    protected Guid getImageContainerId() {
        return getParameters() != null ? getParameters().getVmTemplateId() : super.getImageContainerId();
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        Guid storagePoolId = getDiskImage().getstorage_pool_id() != null ? getDiskImage().getstorage_pool_id()
                .getValue() : Guid.Empty;
        Guid imageGroupId = getDiskImage().getId() != null ? getDiskImage().getId()
                : Guid.Empty;
        Guid snapshotId = getDiskImage().getImageId();
        // Create new image group id and image id:
        Guid destinationImageGroupID = Guid.NewGuid();
        setDestinationImageId(Guid.NewGuid());
        getDiskImage().getSnapshots().addAll(
                ImagesHandler.getAllImageSnapshots(getDiskImage().getImageId(), getDiskImage().getit_guid()));

        setDiskImage(getDiskImage().getSnapshots().get(getDiskImage().getSnapshots().size() - 1));
        DiskImage newImage = CloneDiskImage(getDestinationImageId());
        fillVolumeInformation(newImage);

        VDSReturnValue vdsReturnValue = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.CopyImage,
                        new CopyImageVDSCommandParameters(storagePoolId, getParameters().getStorageDomainId(),
                                getParameters().getVmId(), imageGroupId, snapshotId, destinationImageGroupID,
                                getDestinationImageId(), StringUtils.defaultString(newImage.getdescription()), getParameters()
                                        .getDestinationStorageDomainId(), CopyVolumeType.SharedVol, newImage
                                        .getvolume_format(), newImage.getvolume_type(), getDiskImage()
                                        .isWipeAfterDelete(), false, getStoragePool().getcompatibility_version()
                                        .toString()));

        getReturnValue().getInternalTaskIdList().add(
                CreateTask(vdsReturnValue.getCreationInfo(), VdcActionType.AddVmTemplate));

        newImage.setId(destinationImageGroupID);
        newImage.setvm_snapshot_id(getParameters().getVmSnapshotId());
        newImage.setQuotaId(getParameters().getQuotaId());
        newImage.setParentId(ImagesHandler.BlankImageTemplateId);
        newImage.setit_guid(ImagesHandler.BlankImageTemplateId);
        newImage.setstorage_ids(new ArrayList<Guid>(Arrays.asList(getParameters().getDestinationStorageDomainId())));
        newImage.setactive(true);
        saveImage(newImage);
        newImage.setDiskAlias(ImagesHandler.getSuggestedDiskAlias(newImage, getVmTemplateName()));
        getBaseDiskDao().save(newImage);

        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(newImage.getImageId());
        diskDynamic.setactual_size(getDiskImage().getactual_size());
        DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);

        setActionReturnValue(newImage);

        // set source image as locked:
        LockImage();
        setSucceeded(true);
    }

    /**
     * Fill the volume information from the image ancestor (if available, if not then from the father image).
     *
     * @param disk
     *            The disk to fill the volume details in.
     */
    private void fillVolumeInformation(DiskImage disk) {
        DiskImage ancestor = getDiskImageDao().getAncestor(getDiskImage().getImageId());
        if (ancestor == null) {
            log.warnFormat("Can't find ancestor of Disk with ID {0}, using original disk for volume info.",
                    getDiskImage().getImageId());
            ancestor = getDiskImage();
        }
        disk.setvolume_format(ancestor.getvolume_format());
        disk.setvolume_type(ancestor.getvolume_type());
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        VdcActionParametersBase paramsForTask = getParametersForTask(parentCommand,getParameters());
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                paramsForTask, asyncTaskCreationInfo.getStepId(), getCommandId()));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.copyImage, p, false);

        return ret;
    }

    @Override
    protected void EndWithFailure() {
        UnLockImage();
        setVmTemplate(DbFacade.getInstance().getVmTemplateDAO()
                .get(getVmTemplateId()));
        if (getDestinationDiskImage() != null) {
            getBaseDiskDao().remove(getDestinationDiskImage().getId());
            if (DbFacade.getInstance().getDiskImageDynamicDAO().get(getDestinationDiskImage().getImageId()) != null) {
                DbFacade.getInstance().getDiskImageDynamicDAO().remove(getDestinationDiskImage().getImageId());
            }
            getImageDao().remove(getDestinationImageId());
        }
        setSucceeded(true);
    }
}
