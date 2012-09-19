package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateImageTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
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
                createTask(vdsReturnValue.getCreationInfo(), VdcActionType.AddVmTemplate,
                        VdcObjectType.Storage, getParameters().getStorageDomainId(),
                        getParameters().getDestinationStorageDomainId()));

        newImage.setId(destinationImageGroupID);
        newImage.setvm_snapshot_id(getParameters().getVmSnapshotId());
        newImage.setQuotaId(getParameters().getQuotaId());
        newImage.setParentId(ImagesHandler.BlankImageTemplateId);
        newImage.setit_guid(ImagesHandler.BlankImageTemplateId);
        newImage.setstorage_ids(new ArrayList<Guid>(Arrays.asList(getParameters().getDestinationStorageDomainId())));
        newImage.setactive(true);
        saveImage(newImage);
        newImage.setDiskAlias(getDiskImage().getDiskAlias());
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
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.copyImage;
    }

    @Override
    protected void endWithFailure() {
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
