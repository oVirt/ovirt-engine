package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.CloneImageGroupVolumesStructureCommandParameters;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CloneImageGroupVolumesStructureCommand<T extends CloneImageGroupVolumesStructureCommandParameters>
        extends BaseImagesCommand<T> implements SerialChildExecutingCommand {
    public CloneImageGroupVolumesStructureCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setImageId(getParameters().getImageId());
        setImageGroupId(getParameters().getImageGroupID());
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    @Override
    protected void executeCommand() {
        List<DiskImage> images = getDiskImageDao()
                .getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
        ImagesHandler.sortImageList(images);
        getParameters().setImageIds(ImagesHandler.getDiskImageIds(images));
        persistCommand(getParameters().getParentCommand(), getCallback() != null);
        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return new SerialChildCommandsExecutionCallback();
    }

    private Long determineImageInitialSize(Image sourceImage) {
        // We don't support Sparse-RAW volumes on block domains, therefore if the volume is RAW there is no
        // need to pass initial size (it can be only preallocated).
        if (getParameters().getDestFormat() == VolumeFormat.COW &&
                ImagesHandler.isImageInitialSizeSupported(getStorageDomainStatic(getParameters().getDestDomain())
                        .getStorageType())) {
            //TODO: inspect if we can rely on the database to get the actual size.
            DiskImage imageInfoFromStorage = getVolumeInfo(getParameters().getStoragePoolId(), getParameters()
                    .getSrcDomain(), getParameters().getImageGroupID(), sourceImage.getId());
            // When vdsm creates a COW volume with provided initial size the size is multiplied by 1.1 to prevent a
            // case in which we won't have enough space. If the source is already COW we don't need the additional
            // space.
            return sourceImage.getVolumeFormat() == VolumeFormat.COW ?
                    Double.valueOf(Math.ceil(imageInfoFromStorage.getActualSizeInBytes() /
                            StorageConstants.QCOW_OVERHEAD_FACTOR)).longValue() :
                    imageInfoFromStorage.getActualSizeInBytes();
        }

        return null;
    }

    private StorageDomainStatic getStorageDomainStatic(Guid domainId) {
        return getStorageDomainStaticDao().get(domainId);
    }


    @Override
    public boolean performNextOperation(int completedChildren) {
        if (completedChildren == getParameters().getImageIds().size()) {
            return false;
        }

        Guid imageId = getParameters().getImageIds().get(completedChildren);
        log.info("Starting child command {} of {}, image '{}'",
                completedChildren + 1, getParameters().getImageIds().size(), imageId);

        createImage(getDiskImageDao().getSnapshotById(imageId));
        return true;
    }

    private Guid determineSourceImageGroup(DiskImage image) {
        return image.getImageTemplateId() != Guid.Empty ? getImageDao().get(image.getImageTemplateId()).getId() :
                getParameters().getImageGroupID();

    }

    private void createImage(DiskImage image) {
        CreateVolumeContainerCommandParameters parameters = new CreateVolumeContainerCommandParameters(
                getParameters().getStoragePoolId(),
                getParameters().getDestDomain(),
                getParameters().getImageGroupID(),
                image.getImageId(),
                determineSourceImageGroup(image),
                image.getParentId(),
                getParameters().getDestFormat(),
                getParameters().getDescription(),
                image.getSize(),
                determineImageInitialSize(image.getImage()));

        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        runInternalAction(VdcActionType.CreateVolumeContainer, parameters);
    }

    @Override
    public void handleFailure() {
    }
}
