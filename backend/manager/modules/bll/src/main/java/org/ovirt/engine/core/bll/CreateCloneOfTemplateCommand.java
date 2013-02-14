package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateCloneOfTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

/**
 * This command responsible to creating a copy of template image. Usually it
 * will be called during Create Vm From Template.
 */
@InternalCommandAttribute
public class CreateCloneOfTemplateCommand<T extends CreateCloneOfTemplateParameters> extends
        CreateSnapshotFromTemplateCommand<T> {
    public CreateCloneOfTemplateCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected DiskImage cloneDiskImage(Guid newImageGuid) {
        DiskImage returnValue = super.cloneDiskImage(newImageGuid);
        returnValue.setStorageIds(new ArrayList<Guid>(Arrays.asList(getDestinationStorageDomainId())));
        returnValue.setQuotaId(getParameters().getQuotaId());
        // override to have no template
        returnValue.setParentId(VmTemplateHandler.BlankVmTemplateId);
        returnValue.setImageTemplateId(VmTemplateHandler.BlankVmTemplateId);
        if (getParameters().getDiskImageBase() != null) {
            returnValue.setVolumeType(getParameters().getDiskImageBase().getVolumeType());
            returnValue.setvolumeFormat(getParameters().getDiskImageBase().getVolumeFormat());
        }
        return returnValue;
    }

    @Override
    protected void checkImageValidity() {
        // don't do nothing, overriding base to avoid this check
        // fails when creating vm from template on domain that the template
        // doesn't exist on
    }

    @Override
    protected VDSReturnValue performImageVdsmOperation() {
        setDestinationImageId(Guid.NewGuid());
        mNewCreatedDiskImage = cloneDiskImage(getDestinationImageId());
        mNewCreatedDiskImage.setimage_group_id(Guid.NewGuid());
        Guid storagePoolID = mNewCreatedDiskImage.getStoragePoolId() != null ? mNewCreatedDiskImage
                .getStoragePoolId().getValue() : Guid.Empty;

        VDSReturnValue vdsReturnValue = null;
        try {
            vdsReturnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.CopyImage,
                            new CopyImageVDSCommandParameters(storagePoolID, getDiskImage().getStorageIds().get(0),
                                    getVmTemplateId(), getDiskImage().getimage_group_id().getValue(), getImage()
                                            .getImageId(), mNewCreatedDiskImage.getimage_group_id(), getDestinationImageId(),
                                    "", getDestinationStorageDomainId(),
                                    CopyVolumeType.LeafVol, mNewCreatedDiskImage.getVolumeFormat(),
                                    mNewCreatedDiskImage.getVolumeType(), getDiskImage().isWipeAfterDelete(),
                                    false, getStoragePool().getcompatibility_version().toString()));

            if (vdsReturnValue.getSucceeded()) {
                getReturnValue().getInternalTaskIdList().add(
                        createTask(vdsReturnValue.getCreationInfo(), VdcActionType.AddVmFromTemplate,VdcObjectType.Storage,
                                getParameters().getStorageDomainId(),
                                getDestinationStorageDomainId()));
            }
        } catch (Exception e) {
            log.errorFormat(
                    "CreateCloneOfTemplateCommand::CreateSnapshotInIrsServer::Failed creating snapshot from image id -'{0}'",
                    getImage().getImageId());
            throw new VdcBLLException(VdcBllErrors.VolumeCreationError);
        }
        return vdsReturnValue;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.copyImage;
    }
}
