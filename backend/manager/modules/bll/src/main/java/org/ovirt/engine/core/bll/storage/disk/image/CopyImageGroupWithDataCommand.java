package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.CloneImageGroupVolumesStructureCommandParameters;
import org.ovirt.engine.core.common.action.CopyDataCommandParameters;
import org.ovirt.engine.core.common.action.CopyImageGroupVolumesDataCommandParameters;
import org.ovirt.engine.core.common.action.CopyImageGroupWithDataCommandParameters;
import org.ovirt.engine.core.common.action.CopyImageGroupWithDataCommandParameters.CopyStage;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CopyImageGroupWithDataCommand<T extends CopyImageGroupWithDataCommandParameters>
        extends BaseImagesCommand<T> implements SerialChildExecutingCommand {

    public CopyImageGroupWithDataCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setImageId(getParameters().getImageId());
        setImageGroupId(getParameters().getImageGroupID());
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    @Override
    protected void executeCommand() {
        if (getParameters().isCollapse()) {
            createVolume();
        } else {
            cloneStructureNotCollapsed();
        }

        setSucceeded(true);
    }

    private void cloneStructureNotCollapsed() {
        CloneImageGroupVolumesStructureCommandParameters p = new CloneImageGroupVolumesStructureCommandParameters
                (getParameters().getStoragePoolId(), getParameters().getSrcDomain(), getParameters().getDestDomain(),
                        getParameters().getImageGroupID(), getParameters().getDestinationFormat(), getActionType(),
                        getParameters());
        p.setParentParameters(getParameters());
        p.setParentCommand(getActionType());
        p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        runInternalAction(VdcActionType.CloneImageGroupVolumesStructure, p);
    }

    private void populateDiskSnapshotsInfoFromStorage() {
        getDiskImage().getSnapshots().clear();
        List<DiskImage> images = getDiskImageDao()
                .getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
        for (DiskImage image : images) {
            getDiskImage().getSnapshots().add(getVolumeInfo(getParameters().getStoragePoolId(), getParameters()
                    .getSrcDomain(), getParameters().getImageGroupID(), image.getImageId()));
        }
    }

    private void updateStage(CopyStage stage) {
        getParameters().setStage(stage);
        persistCommand(getParameters().getParentCommand(), getCallback() != null);
    }

    @Override
    public CommandCallback getCallback() {
        return new SerialChildCommandsExecutionCallback();
    }

    private void createVolume() {
        populateDiskSnapshotsInfoFromStorage();
        CreateVolumeContainerCommandParameters parameters = new CreateVolumeContainerCommandParameters(
                getParameters().getStoragePoolId(),
                getParameters().getDestDomain(),
                getParameters().getImageGroupID(),
                getParameters().getImageId(),
                getParameters().getDestImageGroupId(),
                getParameters().getDestinationImageId(),
                getParameters().getDestinationFormat(),
                getParameters().getDescription(),
                getDiskImage().getSize(),
                Double.valueOf(Math.ceil(ImagesHandler.getTotalSizeForClonedDisk(getDiskImage(), getStorageDomain()
                        .getStorageStaticData())) / StorageConstants.QCOW_OVERHEAD_FACTOR).longValue());

        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        runInternalAction(VdcActionType.CreateVolumeContainer, parameters);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getStage() == CopyStage.DEST_CREATION) {
            updateStage(CopyStage.DATA_COPY);
            if (getParameters().isCollapse()) {
                CopyDataCommandParameters parameters = new CopyDataCommandParameters(getParameters().getStoragePoolId(),
                        buildImageLocationInfo(getParameters().getSrcDomain(), getParameters().getImageGroupID(),
                                getParameters().getImageId()),
                        buildImageLocationInfo(getParameters().getDestDomain(), getParameters().getDestImageGroupId(),
                                getParameters().getDestinationImageId()), true);

                parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
                parameters.setParentCommand(getActionType());
                parameters.setParentParameters(getParameters());
                runInternalAction(VdcActionType.CopyData, parameters);
            } else {
                CopyImageGroupVolumesDataCommandParameters p = new CopyImageGroupVolumesDataCommandParameters(
                        getParameters().getStoragePoolId(),
                        getParameters().getSrcDomain(),
                        getParameters().getImageGroupID(),
                        getParameters().getDestDomain(),
                        getActionType(),
                        getParameters()
                );
                p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
                runInternalAction(VdcActionType.CopyImageGroupVolumesData, p);
            }
            return true;
        }
        return false;
    }


    private LocationInfo buildImageLocationInfo(Guid domId, Guid imageGroupId, Guid imageId) {
        return new VdsmImageLocationInfo(domId, imageGroupId, imageId);
    }
}
