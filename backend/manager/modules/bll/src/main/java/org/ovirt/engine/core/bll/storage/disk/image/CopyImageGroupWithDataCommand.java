package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
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

    @Override
    protected void executeCommand() {
        prepareParameters();
        persistCommandIfNeeded();
        if (getParameters().isCollapse()) {
            createVolume();
        } else {
            cloneStructureNotCollapsed();
        }

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
        p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        p.setJobWeight(getParameters().getOperationsJobWeight().get(CopyStage.DEST_CREATION.name()));
        runInternalAction(VdcActionType.CloneImageGroupVolumesStructure, p);
    }

    private void populateDiskSnapshotsInfoFromStorage() {
        getDiskImage().getSnapshots().clear();
        List<DiskImage> images = diskImageDao.getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
        for (DiskImage image : images) {
            getDiskImage().getSnapshots().add(ImagesHandler.getVolumeInfoFromVdsm(getParameters().getStoragePoolId(),
                    getParameters().getSrcDomain(), getParameters().getImageGroupID(), image.getImageId()));
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
                Guid.Empty,
                Guid.Empty,
                getParameters().getDestImageGroupId(),
                getParameters().getDestinationImageId(),
                getParameters().getDestinationFormat(),
                getParameters().getDescription(),
                getDiskImage().getSize(),
                ImagesHandler.determineTotalImageInitialSize(getDiskImage(),
                        getParameters().getDestinationFormat(),
                        getParameters().getSrcDomain(),
                        getParameters().getDestDomain()));

        parameters.setJobWeight(getParameters().getOperationsJobWeight().get(CopyStage.DEST_CREATION.name()));
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        runInternalAction(VdcActionType.CreateVolumeContainer, parameters);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getStage() == CopyStage.DEST_CREATION) {
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
                p.setJobWeight(weight);
                runInternalAction(VdcActionType.CopyImageGroupVolumesData, p);
            }
            return true;
        }
        return false;
    }


    private LocationInfo buildImageLocationInfo(Guid domId, Guid imageGroupId, Guid imageId) {
        return new VdsmImageLocationInfo(domId, imageGroupId, imageId, null);
    }

    private DiskImage getDiskImage() {
        if (diskImage == null) {
            diskImage = (DiskImage) diskDao.get(getParameters().getImageGroupID());
        }

        return diskImage;
    }
}
