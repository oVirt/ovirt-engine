package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CloneImageGroupVolumesStructureCommandParameters;
import org.ovirt.engine.core.common.action.CopyDataCommandParameters;
import org.ovirt.engine.core.common.action.CopyImageGroupVolumesDataCommandParameters;
import org.ovirt.engine.core.common.action.CopyImageGroupWithDataCommandParameters;
import org.ovirt.engine.core.common.action.CopyImageGroupWithDataCommandParameters.CopyStage;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.action.MeasureVolumeParameters;
import org.ovirt.engine.core.common.action.UpdateVolumeCommandParameters;
import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CopyImageGroupWithDataCommand<T extends CopyImageGroupWithDataCommandParameters>
        extends CommandBase<T> implements SerialChildExecutingCommand {

    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;
    @Inject
    private ImagesHandler imagesHandler;

    private DiskImage diskImage;

    public CopyImageGroupWithDataCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getStoragePoolId());
        setStorageDomainId(getParameters().getDestDomain());
    }

    private void prepareParameters() {
        if (getParameters().getJobWeight() != null) {
            Map<String, Integer> weights = new HashMap<>();
            int createWeight = Long.valueOf(Math.round(getParameters().getJobWeight() / 10d)).intValue();
            weights.put(CopyStage.DEST_CREATION.name(), createWeight);
            weights.put(CopyStage.DATA_COPY.name(), getParameters().getJobWeight() - createWeight);
            getParameters().setOperationsJobWeight(weights);
        }
    }

    protected void createVolumes() {
        if (getParameters().isCollapse()) {
            createVolume();
        } else {
            cloneStructureNotCollapsed();
        }
    }

    @Override
    protected void executeCommand() {
        prepareParameters();
        persistCommandIfNeeded();
        createVolumes();

        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    private void cloneStructureNotCollapsed() {
        CloneImageGroupVolumesStructureCommandParameters p = new CloneImageGroupVolumesStructureCommandParameters
                (getParameters().getStoragePoolId(), getParameters().getSrcDomain(), getParameters().getDestDomain(),
                        getParameters().getImageGroupID(), getActionType(), getParameters());
        p.setParentParameters(getParameters());
        p.setParentCommand(getActionType());
        p.setDestImageGroupId(getParameters().getDestImageGroupId());
        p.setDestImageId(getParameters().getDestinationImageId());
        p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        p.setJobWeight(getParameters().getOperationsJobWeight().get(CopyStage.DEST_CREATION.name()));
        p.setDestImages(getParameters().getDestImages());
        runInternalAction(ActionType.CloneImageGroupVolumesStructure, p);
    }

    private void populateDiskSnapshotsInfoFromStorage() {
        getDiskImage().getSnapshots().clear();
        List<DiskImage> images = diskImageDao.getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
        for (DiskImage image : images) {
            getDiskImage().getSnapshots().add(imagesHandler.getVolumeInfoFromVdsm(getParameters().getStoragePoolId(),
                    getParameters().getSrcDomain(), getParameters().getImageGroupID(), image.getImageId()));
        }
    }

    private void updateStage(CopyStage stage) {
        getParameters().setStage(stage);
        persistCommand(getParameters().getParentCommand(), getCallback() != null);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    private void createVolume() {
        if (getActionType() != ActionType.CopyManagedBlockDisk) {
            populateDiskSnapshotsInfoFromStorage();
        }

        CreateVolumeContainerCommandParameters parameters = new CreateVolumeContainerCommandParameters(
                getParameters().getStoragePoolId(),
                getParameters().getDestDomain(),
                Guid.Empty,
                Guid.Empty,
                getParameters().getDestImageGroupId(),
                getParameters().getDestinationImageId(),
                getParameters().getDestinationFormat(),
                getParameters().getDestinationVolumeType(),
                getParameters().getDescription(),
                getDiskImage().getSize(),
                determineTotalImageInitialSize(getDiskImage(),
                        getParameters().getDestinationFormat(),
                        getParameters().getSrcDomain()));
        parameters.setDiskAlias(getParameters().getDiskAlias());
        parameters.setJobWeight(getParameters().getOperationsJobWeight().get(CopyStage.DEST_CREATION.name()));
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        runInternalAction(ActionType.CreateVolumeContainer, parameters);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getStage() == CopyStage.DEST_CREATION) {
            copyData();
            return true;
        } else if (getParameters().getStage() == CopyStage.DATA_COPY) {
            updateStage(CopyStage.UPDATE_VOLUME);

            // There is no need to update the volume if we are creating a cloned VM from template
            if (!isTemplate(getDiskImage()) || getParameters().getParentCommand() == ActionType.CreateCloneOfTemplate) {
                return true;
            }

            UpdateVolumeCommandParameters parameters = new UpdateVolumeCommandParameters(
                    getParameters().getStoragePoolId(),
                    // vol_info
                    (VdsmImageLocationInfo) buildImageLocationInfo(getParameters().getDestDomain(),
                            getParameters().getDestImageGroupId(),
                            getParameters().getDestinationImageId()),
                    // legality
                    null,
                    // description
                    null,
                    // generation
                    null,
                    // VolumeRole, true will set the volume as SHARED
                    Boolean.TRUE);

            parameters.setParentCommand(getActionType());
            parameters.setParentParameters(getParameters());
            parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);

            runInternalActionWithTasksContext(ActionType.UpdateVolume, parameters);

            return true;
        }

        return false;
    }

    protected void copyData() {
        updateStage(CopyStage.DATA_COPY);
        Integer weight = getParameters().getOperationsJobWeight().get(CopyStage.DATA_COPY.name());

        if (getParameters().isCollapse()) {
            CopyDataCommandParameters parameters = new CopyDataCommandParameters(getParameters().getStoragePoolId(),
                    buildImageLocationInfo(getParameters().getSrcDomain(), getParameters().getImageGroupID(),
                            getParameters().getImageId()),
                    buildImageLocationInfo(getParameters().getDestDomain(), getParameters().getDestImageGroupId(),
                            getParameters().getDestinationImageId()), true);

            parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
            parameters.setParentCommand(getActionType());
            parameters.setParentParameters(getParameters());
            parameters.setJobWeight(weight);
            runInternalAction(ActionType.CopyData, parameters);
        } else {
            CopyImageGroupVolumesDataCommandParameters p = new CopyImageGroupVolumesDataCommandParameters(
                    getParameters().getStoragePoolId(),
                    getParameters().getSrcDomain(),
                    getParameters().getImageGroupID(),
                    getParameters().getDestDomain(),
                    getActionType(),
                    getParameters()
            );
            p.setDestImageGroupId(getParameters().getDestImageGroupId());
            p.setDestImageId(getParameters().getDestinationImageId());
            p.setDestImages(getParameters().getDestImages());
            p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
            p.setJobWeight(weight);
            runInternalAction(ActionType.CopyImageGroupVolumesData, p);
        }
    }

    private Long determineTotalImageInitialSize(DiskImage sourceImage,
            VolumeFormat destFormat,
            Guid srcDomain) {
        // Check if we have a host in the DC capable of running the measure volume verb,
        // otherwise fallback to the legacy method
        Guid hostId = imagesHandler.getHostForMeasurement(sourceImage.getStoragePoolId(),
                sourceImage.getId());
        // We are collapsing the chain, so we want to measure the leaf to get the size
        // of the entire chain
        List<DiskImage> images = diskImageDao.getAllSnapshotsForImageGroup(sourceImage.getId());
        imagesHandler.sortImageList(images);
        DiskImage leaf = images.get(images.size() - 1);
        if (hostId == null || (leaf.getActive() && !leaf.getStorageTypes().get(0).isBlockDomain())) {
            return imagesHandler.determineTotalImageInitialSize(getDiskImage(),
                    getParameters().getDestinationFormat(),
                    getParameters().getSrcDomain(),
                    getParameters().getDestDomain());
        } else {
            MeasureVolumeParameters parameters = new MeasureVolumeParameters(leaf.getStoragePoolId(),
                    srcDomain,
                    leaf.getId(),
                    leaf.getImageId(),
                    destFormat.getValue());
            parameters.setParentCommand(getActionType());
            parameters.setEndProcedure(EndProcedure.PARENT_MANAGED);
            parameters.setVdsRunningOn(hostId);
            parameters.setCorrelationId(getCorrelationId());
            ActionReturnValue actionReturnValue =
                    runInternalAction(ActionType.MeasureVolume, parameters,
                            ExecutionHandler.createDefaultContextForTasks(getContext()));

            if (!actionReturnValue.getSucceeded()) {
                throw new RuntimeException("Could not measure volume");
            }

            return actionReturnValue.getActionReturnValue();
        }
    }

    private LocationInfo buildImageLocationInfo(Guid domId, Guid imageGroupId, Guid imageId) {
        return new VdsmImageLocationInfo(domId, imageGroupId, imageId, null);
    }

    private boolean isTemplate(DiskImage diskImage) {
        return diskImage.getVmEntityType() != null && diskImage.getVmEntityType().isTemplateType();
    }

    private DiskImage getDiskImage() {
        if (diskImage == null) {
            diskImage = (DiskImage) diskDao.get(getParameters().getImageGroupID());
        }

        return diskImage;
    }
}
