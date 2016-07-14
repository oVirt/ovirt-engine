package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Optional;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.CreateSnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CreateVolumeContainerCommand<T extends CreateVolumeContainerCommandParameters> extends
        BaseImagesCommand<T> {

    public CreateVolumeContainerCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public CreateVolumeContainerCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.createVolume;
    }

    private CreateImageVDSCommandParameters getCreateVDSCommandParameters() {
        CreateSnapshotVDSCommandParameters parameters =
                new CreateSnapshotVDSCommandParameters(getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(), getParameters().getImageGroupID(), getParameters()
                        .getSrcImageId(), getParameters().getSize(),
                        VolumeType.Sparse, getParameters().getVolumeFormat(), getParameters().getSrcImageGroupId(),
                        getParameters().getImageId(),
                        getParameters().getDescription());
        parameters.setImageInitialSizeInBytes(Optional.ofNullable(getParameters().getInitialSize()).orElse(0L));
        return parameters;
    }

    @Override
    protected void executeCommand() {
        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.CreateSnapshot,
                getCreateVDSCommandParameters());
        if (vdsReturnValue.getSucceeded()) {
            getParameters().setVdsmTaskIds(new ArrayList<>());
            getParameters().getVdsmTaskIds().add(
                    createTask(taskId,
                            vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));
            getTaskIdList().add(getParameters().getVdsmTaskIds().get(0));
            setSucceeded(true);
        }
    }
}

