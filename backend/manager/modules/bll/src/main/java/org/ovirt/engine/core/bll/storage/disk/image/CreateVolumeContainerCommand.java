package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.vdscommands.CreateVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CreateVolumeContainerCommand<T extends CreateVolumeContainerCommandParameters> extends
        CommandBase<T> {

    @Inject
    private ImagesHandler imagesHandler;

    public CreateVolumeContainerCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStorageDomainId(getParameters().getStorageDomainId());
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    public CreateVolumeContainerCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.createVolume;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    private CreateVolumeVDSCommandParameters getCreateVDSCommandParameters() {
        DiskImage diskImageDescription = new DiskImage();
        diskImageDescription.setDiskAlias(getParameters().getDiskAlias());
        diskImageDescription.setDiskDescription(getParameters().getDescription());

        CreateVolumeVDSCommandParameters parameters =
                new CreateVolumeVDSCommandParameters(getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getImageGroupID(),
                        getParameters().getSrcImageId(),
                        getParameters().getSize(),
                        getType(),
                        getParameters().getVolumeFormat(),
                        getParameters().getSrcImageGroupId(),
                        getParameters().getImageId(),
                        imagesHandler.getJsonDiskDescription(diskImageDescription),
                        getStoragePool().getCompatibilityVersion(),
                        DiskContentType.DATA);
        if (getType() != VolumeType.Preallocated && getStorageDomain().getStorageType().isBlockDomain()) {
            parameters.setImageInitialSizeInBytes(Optional.ofNullable(getParameters().getInitialSize()).orElse(0L));
        }
        return parameters;
    }

    private VolumeType getType() {
        // For raw volume on file-based domain we cannot determine
        // the type according to the format because since 4.3 engine support
        // raw-preallocate file volume on a file-based storage domain
        if (getParameters().getVolumeFormat() == VolumeFormat.RAW) {
            return getStorageDomain().getStorageType().isBlockDomain() ?
                    VolumeType.Preallocated :
                    getParameters().getVolumeType();
        }
        return VolumeType.Sparse;
    }

    @Override
    protected void executeCommand() {
        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.CreateVolume,
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

    @Override
    public List<StepSubjectEntity> getCommandStepSubjectEntities() {
        if (getParameters().getJobWeight() != null) {
            return Collections.singletonList(new StepSubjectEntity(VdcObjectType.Disk,
                    getParameters().getImageGroupID(), getParameters().getJobWeight()));
        }

        return super.getCommandStepSubjectEntities();
    }
}

