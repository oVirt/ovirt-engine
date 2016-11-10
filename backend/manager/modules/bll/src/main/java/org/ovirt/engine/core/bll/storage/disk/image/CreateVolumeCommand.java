package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.AllocateImageGroupVolumeCommandParameters;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.action.CreateVolumeParameters;
import org.ovirt.engine.core.common.action.CreateVolumeParameters.CreationState;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateVolumeCommand<T extends CreateVolumeParameters> extends CommandBase<T> implements
        SerialChildExecutingCommand {

    public CreateVolumeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private boolean preallocate() {
        return getParameters().getVolumeType() == VolumeType.Preallocated
                && getStorageDomain().getStorageType().isFileDomain();
    }

    @Override
    protected void executeCommand() {
        createImage();
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    private void updateStage(CreationState stage) {
        getParameters().setCreationState(stage);
        persistCommand(getParameters().getParentCommand(), getCallback() != null);
    }

    @Override
    public CommandCallback getCallback() {
        return new SerialChildCommandsExecutionCallback();
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getCreationState() == CreationState.VOLUME_CREATION && preallocate()) {
            updateStage(CreationState.VOLUME_ALLOCATION);
            allocateImage();
            return true;
        }

        return false;
    }

    private void allocateImage() {
        AllocateImageGroupVolumeCommandParameters parameters = new AllocateImageGroupVolumeCommandParameters
                (getParameters().getStoragePoolId(), getParameters()
                        .getStorageDomainId(), getParameters().getNewImageGroupId(), getParameters().getNewImageId(),
                        getParameters().getImageSizeInBytes());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        runInternalAction(VdcActionType.AllocateImageGroupVolume, parameters);
    }

    private void createImage() {
        CreateVolumeContainerCommandParameters parameters = new CreateVolumeContainerCommandParameters(
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getSrcImageGroupId(),
                getParameters().getSrcImageId(),
                getParameters().getNewImageGroupId(),
                getParameters().getNewImageId(),
                getParameters().getVolumeFormat(),
                getParameters().getDescription(),
                getParameters().getImageSizeInBytes(),
                getParameters().getInitialSize());

        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        runInternalAction(VdcActionType.CreateVolumeContainer, parameters);
    }

    @Override
    public void handleFailure() {

    }
}
