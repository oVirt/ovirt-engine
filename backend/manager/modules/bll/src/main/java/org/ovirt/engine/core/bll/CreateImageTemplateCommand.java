package org.ovirt.engine.core.bll;

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
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This command responcible to create new Image Template from image.
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
    protected String CalculateImageDescription() {
        return String.format("_%1$s_%2$s_template", getVmTemplateName(), new java.util.Date());
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        Guid storagePoolId = getDiskImage().getstorage_pool_id() != null ? getDiskImage().getstorage_pool_id()
                .getValue() : Guid.Empty;
        Guid imageGroupId = getDiskImage().getimage_group_id() != null ? getDiskImage().getimage_group_id().getValue()
                : Guid.Empty;
        Guid snapshotId = getDiskImage().getId();
        // Create new image group id and image id:
        Guid destinationImageGroupID = Guid.NewGuid();
        setDestinationImageId(Guid.NewGuid());
        getDiskImage().getSnapshots().addAll(
                ImagesHandler.getAllImageSnapshots(getDiskImage().getId(), getDiskImage().getit_guid()));

        setDiskImage(getDiskImage().getSnapshots().get(getDiskImage().getSnapshots().size() - 1));
        DiskImage newImage = CloneDiskImage(getDestinationImageId());
        newImage.setstorage_id(getParameters().getDestinationStorageDomainId());
        fillVolumeInformation(newImage);

        VDSReturnValue vdsReturnValue = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.CopyImage,
                        new CopyImageVDSCommandParameters(storagePoolId, getParameters().getStorageDomainId(),
                                getParameters().getVmId(), imageGroupId, snapshotId, destinationImageGroupID,
                                getDestinationImageId(), newImage.getdescription(), getParameters()
                                        .getDestinationStorageDomainId(), CopyVolumeType.SharedVol, newImage
                                        .getvolume_format(), newImage.getvolume_type(), getDiskImage()
                                        .getwipe_after_delete(), false, getStoragePool().getcompatibility_version()
                                        .toString()));

        getReturnValue().getInternalTaskIdList().add(
                CreateTask(vdsReturnValue.getCreationInfo(), VdcActionType.AddVmTemplate));

        newImage.setimage_group_id(destinationImageGroupID);
        newImage.setvm_snapshot_id(getParameters().getVmSnapshotId());
        newImage.setParentId(ImagesHandler.BlankImageTemplateId);
        newImage.setit_guid(ImagesHandler.BlankImageTemplateId);

        // Note ImageGuid copied to DiskImage template twise due bug:
        // 1. as vtmid_guid
        // 2. as it_guid
        // TODO: review it after bug will be fixed
        DiskImageTemplate dt = new DiskImageTemplate(newImage.getId(), getImageContainerId(),
                newImage.getinternal_drive_mapping(), newImage.getId(), "", "", getNow(),
                newImage.getactual_size(), newImage.getdescription(), null);
        DbFacade.getInstance().getDiskImageTemplateDAO().save(dt);
        DbFacade.getInstance().getDiskImageDAO().save(newImage);
        DbFacade.getInstance().getDiskDao().save(newImage.getDisk());

        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(newImage.getId());
        diskDynamic.setactual_size(getDiskImage().getactual_size());
        DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);

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
        DiskImage ancestor = DbFacade.getInstance().getDiskImageDAO().getAncestor(getDiskImage().getId());
        if (ancestor == null) {
            log.warnFormat("Can't find ancestor of Disk with ID {0}, using original disk for volume info.",
                    getDiskImage().getId());
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
                paramsForTask));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.copyImage, p, false);

        return ret;
    }

    @Override
    protected void EndWithFailure() {
        UnLockImage();
        setVmTemplate(DbFacade.getInstance().getVmTemplateDAO()
                .get(getVmTemplateId()));

        DbFacade.getInstance().getDiskImageTemplateDAO().remove(getDestinationImageId());
        DbFacade.getInstance().getDiskImageDAO().remove(getDestinationImageId());
        if (getDestinationDiskImage() != null) {
            DbFacade.getInstance().getDiskDao().remove(getDestinationDiskImage().getimage_group_id());
            if (DbFacade.getInstance().getDiskImageDynamicDAO().get(getDestinationDiskImage().getId()) != null) {
                DbFacade.getInstance().getDiskImageDynamicDAO().remove(getDestinationDiskImage().getId());
            }
        }
        setSucceeded(true);
    }
}
