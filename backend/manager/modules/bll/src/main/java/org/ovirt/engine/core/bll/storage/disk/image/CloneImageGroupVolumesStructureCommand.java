package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.CommandsWeightsUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CloneImageGroupVolumesStructureCommandParameters;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.action.MeasureVolumeParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.utils.CollectionUtils;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CloneImageGroupVolumesStructureCommand<T extends CloneImageGroupVolumesStructureCommandParameters> extends CommandBase<T> implements SerialChildExecutingCommand {

    @Inject
    private CommandsWeightsUtils commandsWeightsUtils;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    @Inject
    private ImagesHandler imagesHandler;

    public CloneImageGroupVolumesStructureCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    @Override
    protected void executeCommand() {
        // If we are copying a template we will get the same disk multiple times
        List<DiskImage> images = diskImageDao.getAllSnapshotsForImageGroup(getParameters().getImageGroupID())
                .stream()
                .filter(CollectionUtils.distinctByKey(DiskImage::getImageId))
                .collect(Collectors.toList());

        ImagesHandler.sortImageList(images);
        getParameters().setImageIds(ImagesHandler.getDiskImageIds(images));
        prepareWeights();
        persistCommand(getParameters().getParentCommand(), getCallback() != null);
        setSucceeded(true);
    }

    private void prepareWeights() {
        if (getParameters().getJobWeight() == null) {
            return;
        }

        Double imageWeight = 1d / getParameters().getImageIds().size();
        List<Guid> imageIds = getParameters().getDestImages().isEmpty() ?
                getParameters().getImageIds() :
                getParameters().getDestImages()
                        .stream()
                        .map(d -> d.getImageId())
                        .collect(Collectors.toList());

        Map<String, Double> weightDivision =
                imageIds.stream().collect(Collectors.toMap(Guid::toString, z -> imageWeight));

        getParameters()
                .setOperationsJobWeight(commandsWeightsUtils.adjust(weightDivision, getParameters().getJobWeight()));
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public boolean performNextOperation(int completedChildren) {
        if (completedChildren == getParameters().getImageIds().size()) {
            return false;
        }

        Guid imageId = getParameters().getDestImages().isEmpty() ?
                getParameters().getImageIds().get(completedChildren) :
                getParameters().getDestImages().get(completedChildren).getImageId();
        log.info("Starting child command {} of {}, image '{}'",
                completedChildren + 1,
                getParameters().getImageIds().size(),
                imageId);

        if (!getParameters().getDestImages().isEmpty()) {
            createImage(getParameters().getDestImages().get(completedChildren), completedChildren);
        } else {
            createImage(diskImageDao.getSnapshotById(imageId), completedChildren);
        }
        return true;
    }

    private Guid determineSourceImageGroup(DiskImage image) {
        if (Guid.Empty.equals(image.getParentId())) {
            return Guid.Empty;
        } else if (image.getImageTemplateId().equals(image.getParentId())) {
            return imageDao.get(image.getImageTemplateId()).getDiskId();
        } else if (!getParameters().getDestImages().isEmpty()) {
            return getParameters().getDestImageGroupId();
        }

        return getParameters().getImageGroupID();

    }

    private void createImage(DiskImage image, int imageIndex) {
        VolumeFormat volumeFormat = determineVolumeFormat(getParameters().getDestDomain(),
                image.getVolumeFormat(),
                image.getVolumeType());

        Image innerImage = image.getImage();
        if (!getParameters().getDestImages().isEmpty()) {
            innerImage = diskImageDao.getSnapshotById(getParameters().getImageIds()
                    .get(imageIndex))
                    .getImage();
        }

        Long initialSize = determineImageInitialSize(innerImage,
                volumeFormat,
                getParameters().getStoragePoolId(),
                getParameters().getSrcDomain(),
                getParameters().getDestDomain(),
                getParameters().getImageGroupID());

        CreateVolumeContainerCommandParameters parameters = new CreateVolumeContainerCommandParameters(
                getParameters().getStoragePoolId(),
                getParameters().getDestDomain(),
                determineSourceImageGroup(image),
                image.getParentId(),
                getParameters().getDestImageGroupId(),
                image.getImageId(),
                volumeFormat,
                image.getVolumeType(),
                getParameters().getDescription(),
                image.getSize(),
                initialSize);

        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setJobWeight(getParameters().getOperationsJobWeight().get(image.getImageId().toString()));
        runInternalActionWithTasksContext(ActionType.CreateVolumeContainer, parameters);
    }

    private Long determineImageInitialSize(Image sourceImage,
            VolumeFormat destFormat,
            Guid storagePoolId,
            Guid srcDomain,
            Guid dstDomain,
            Guid imageGroupID) {
        Guid hostId = imagesHandler.getHostForMeasurement(storagePoolId, imageGroupID);
        if (hostId != null) {
            if ((storageDomainDao.get(srcDomain).getStorageType().isBlockDomain() || !sourceImage.isActive()) &&
                    storageDomainDao.get(dstDomain).getStorageType().isBlockDomain()) {
                MeasureVolumeParameters parameters = new MeasureVolumeParameters(storagePoolId,
                        srcDomain,
                        imageGroupID,
                        sourceImage.getId(),
                        destFormat.getValue());
                parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
                parameters.setParentCommand(getActionType());
                parameters.setVdsRunningOn(hostId);
                parameters.setCorrelationId(getCorrelationId());
                parameters.setWithBacking(false);
                ActionReturnValue actionReturnValue =
                        runInternalAction(ActionType.MeasureVolume, parameters,
                                ExecutionHandler.createDefaultContextForTasks(getContext()));
                if (!actionReturnValue.getSucceeded()) {
                    throw new RuntimeException("Could not measure volume");
                }

                long requiredSize = actionReturnValue.getActionReturnValue();

                // The required size for the leaf might be very small depending on the amount of data.
                // Extend it to 1GB or disk size to avoid having the VM paused for extension too fast.
                if (sourceImage.isActive()) {

                    // TODO: 1GB is selected as this is the default chunk size in Vdsm.
                    // This uses the logic from Vdsm's optimal_size
                    // https://github.com/oVirt/vdsm/blob/e11b71eab5995fb00e5fe1619332687a5f717903/lib/vdsm/storage/blockVolume.py#L403
                    // Ideally, we'd get a report from Vdsm about the chunk size and use it rather than assuming
                    // it is the default chunk size.
                    // https://bugzilla.redhat.com/show_bug.cgi?id=1993839
                    requiredSize += SizeConverter.BYTES_IN_GB;
                    return Math.min(requiredSize, sourceImage.getSize());
                }

                return requiredSize;
            }
        }

        // We don't support Sparse-RAW volumes on block domains, therefore if the volume is RAW there is no
        // need to pass initial size (it can be only preallocated).
        if (imagesHandler.isInitialSizeSupportedForFormat(destFormat, dstDomain)) {
            //TODO: inspect if we can rely on the database to get the actual size.
            DiskImage imageInfoFromStorage = imagesHandler.getVolumeInfoFromVdsm(storagePoolId,
                    srcDomain, imageGroupID, sourceImage.getId());

            return ImagesHandler.computeCowImageNeededSize(sourceImage.getVolumeFormat(),
                    imageInfoFromStorage.getActualSizeInBytes());
        }

        return null;
    }

    private VolumeFormat determineVolumeFormat(Guid destStorageDomainId,
            VolumeFormat srcVolumeFormat,
            VolumeType srcVolumeType) {
        // Block storage domain does not support raw/thin disk.
        // File based raw/thin disk will convert to cow/sparse
        if (srcVolumeFormat.equals(VolumeFormat.RAW) && srcVolumeType.equals(VolumeType.Sparse)) {
            StorageDomainStatic destStorageDomain = storageDomainStaticDao.get(destStorageDomainId);
            if (destStorageDomain.getStorageType().isBlockDomain()) {
                return VolumeFormat.COW;
            }
        }
        return srcVolumeFormat;
    }

    @Override
    public void handleFailure() {
    }
}
