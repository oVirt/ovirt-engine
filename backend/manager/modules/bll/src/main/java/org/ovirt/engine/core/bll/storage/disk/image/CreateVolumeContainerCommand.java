package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.CreateVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CreateVolumeContainerCommand<T extends CreateVolumeContainerCommandParameters> extends
        CommandBase<T> {

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

    private CreateImageVDSCommandParameters getCreateVDSCommandParameters() {
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
                        getParameters().getDescription(),
                        getStoragePool().getCompatibilityVersion(),
                        DiskContentType.DATA);
        if (getType() != VolumeType.Preallocated && getStorageDomain().getStorageType().isBlockDomain()) {
            parameters.setImageInitialSizeInBytes(Optional.ofNullable(getParameters().getInitialSize()).orElse(0L));
        } else if (supportsDeferredPrallocation()) {
            // In case of move/copy of preallocated disk on file storage domain,
            // the initial volume will be created with the minimum initial size in order
            // to reduce volume creation time.
            // Later, the engine starts copy_data job on VDSM that invoke qemuimg.convert()
            // with preallocation="falloc" and allocates the entire volume size.
            Long initialSize = Optional.ofNullable(getParameters().getInitialSize()).orElse(0L);
            parameters.setImageInitialSizeInBytes(initialSize);
        }
        return parameters;
    }

    private boolean supportsDeferredPrallocation() {
        return getStorageDomain().getStorageType().isFileDomain() &&
                getType() == VolumeType.Preallocated &&
                FeatureSupported.isDeferringFileVolumePreallocationSupported(
                        getStoragePool().getCompatibilityVersion());
    }

    private VolumeType getType() {
        if (getStorageDomain().getStorageType().isInternal() &&
                getParameters().getVolumeFormat() == VolumeFormat.RAW) {
            return VolumeType.Preallocated;
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

