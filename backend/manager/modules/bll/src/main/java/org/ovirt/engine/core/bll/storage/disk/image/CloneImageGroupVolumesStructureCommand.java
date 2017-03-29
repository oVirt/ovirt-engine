package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.CommandsWeightsUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.CloneImageGroupVolumesStructureCommandParameters;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CloneImageGroupVolumesStructureCommand<T extends CloneImageGroupVolumesStructureCommandParameters> extends CommandBase<T> implements SerialChildExecutingCommand {

    @Inject
    private CommandsWeightsUtils commandsWeightsUtils;

    @Inject
    private ImagesHandler imagesHandler;

    public CloneImageGroupVolumesStructureCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    @Override
    protected void executeCommand() {
        List<DiskImage> images = diskImageDao.getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
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
        Map<String, Double> weightDivision =
                getParameters().getImageIds().stream().collect(Collectors.toMap(Guid::toString, z -> imageWeight));

        getParameters()
                .setOperationsJobWeight(commandsWeightsUtils.adjust(weightDivision, getParameters().getJobWeight()));
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public CommandCallback getCallback() {
        return new SerialChildCommandsExecutionCallback();
    }

    @Override
    public boolean performNextOperation(int completedChildren) {
        if (completedChildren == getParameters().getImageIds().size()) {
            return false;
        }

        Guid imageId = getParameters().getImageIds().get(completedChildren);
        log.info("Starting child command {} of {}, image '{}'",
                completedChildren + 1,
                getParameters().getImageIds().size(),
                imageId);

        createImage(diskImageDao.getSnapshotById(imageId));
        return true;
    }

    private Guid determineSourceImageGroup(DiskImage image) {
        if (Guid.Empty.equals(image.getParentId())) {
            return Guid.Empty;
        }

        return image.getImageTemplateId().equals(image.getParentId()) ?
                imageDao.get(image.getImageTemplateId()).getDiskId()
                : getParameters().getImageGroupID();

    }

    private void createImage(DiskImage image) {
        CreateVolumeContainerCommandParameters parameters = new CreateVolumeContainerCommandParameters(
                getParameters().getStoragePoolId(),
                getParameters().getDestDomain(),
                determineSourceImageGroup(image),
                image.getParentId(),
                getParameters().getImageGroupID(),
                image.getImageId(),
                image.getVolumeFormat(),
                getParameters().getDescription(),
                image.getSize(),
                imagesHandler.determineImageInitialSize(image.getImage(),
                        image.getVolumeFormat(),
                        getParameters().getStoragePoolId(),
                        getParameters().getSrcDomain(),
                        getParameters().getDestDomain(),
                        getParameters().getImageGroupID()));

        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setJobWeight(getParameters().getOperationsJobWeight().get(image.getImageId().toString()));
        runInternalActionWithTasksContext(VdcActionType.CreateVolumeContainer, parameters);
    }

    @Override
    public void handleFailure() {
    }
}
