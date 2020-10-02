package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RefreshVolumeParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ReduceImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class ReduceImageCommand<T extends ImagesActionsParametersBase> extends BaseImagesCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(ReduceImageCommand.class);

    @Inject
    private ImagesHandler imagesHandler;
    @Inject
    private VmDao vmDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private DiskImageDao diskImageDao;

    public ReduceImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setImageId(getParameters().getImageId());
    }

    @Override
    public void init() {
        super.init();
        if (getDiskImage() == null) {
            return;
        }
        setStorageDomainId(getDiskImage().getStorageIds().get(0));
        setStoragePoolId(getDiskImage().getStoragePoolId());
        setImageGroupId(getDiskImage().getId());
        setVm(vmDao
                .getVmsWithPlugInfo(getDiskImage().getId())
                .stream()
                .filter(p -> p.getSecond().isPlugged())
                .map(Pair::getFirst)
                .findFirst()
                .orElse(null));
        if (getVm() != null && getVm().isRunning()) {
            setVdsId(getVm().getRunOnVds());
        }
    }

    @Override
    protected boolean validate() {
        DiskValidator diskValidator = createDiskValidator();
        if (!validate(diskValidator.isDiskExists())) {
            return false;
        }
        if (getDiskImage().isShareable()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_REDUCE_IMAGE_NOT_SUPPORTED_FOR_SHAREABLE_DISK);
        }
        if (getStorageDomain().getStorageType().isFileDomain()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_REDUCE_IMAGE_NOT_SUPPORTED_FOR_FILE_DOMAINS);
        }
        if (getVm() != null && getVm().isRunning() && isActiveImage()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_REDUCE_IMAGE_NOT_SUPPORTED_FOR_ACTIVE_IMAGE);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.Disk, getImageGroupId()));

        boolean prepareImageSucceeded = false;
        boolean reduceImageSucceeded = false;

        if (!isVmRunningOnSpm()) {
            // The VM isn't running on the SPM but the reduce command is performed on the SPM, hence
            // we have to prepare the image on the SPM
            log.debug("Preparing image {}/{} on the SPM", getImageGroupId(), getImageId());
            try {
                prepareImage();
                prepareImageSucceeded = true;
            } catch (EngineException e) {
                log.error("Failed to prepare image {}/{} on the SPM", getImageGroupId(), getImageId());
            }
        }

        if (!isVmRunningOnSpm() && !prepareImageSucceeded) {
            // As we don't want to fail the live merge because of a failure to reduce the image, we still mark the
            // command as succeeded.
            setSucceeded(true);
            return;
        }

        try {
            VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.ReduceImage, createReduceImageVDSCommandParameters());
            if (vdsReturnValue.getSucceeded()) {
                Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
                getTaskIdList().add(createTask(taskId,
                        vdsReturnValue.getCreationInfo(),
                        getParameters().getParentCommand(),
                        VdcObjectType.Storage,
                        getParameters().getStorageDomainId()));
                reduceImageSucceeded = true;
            }
        } catch (EngineException e) {
            log.error("Reducing image {}/{} failed", getImageGroupId(), getImageId());
        }

        if (prepareImageSucceeded && !reduceImageSucceeded) {
            try {
                teardownImage();
            } catch (EngineException e) {
                log.error("Failed to teardown image {}/{} on the SPM", getImageGroupId(), getImageId());
            }
        }

        setSucceeded(true);
    }

    protected DiskValidator createDiskValidator() {
        return new DiskValidator(getDiskImage());
    }

    private boolean isVmRunningOnSpm() {
        return getStoragePool().getSpmVdsId().equals(getVdsId());
    }

    @Override
    public ActionReturnValue endAction() {
        if (!isVmRunningOnSpm()) {
            // Teardown the image on the SPM
            log.debug("Tearing down image {}/{} on the SPM", getImageGroupId(), getImageId());
            try {
                teardownImage();
            } catch (EngineException e) {
                log.error("Failed to teardown image {}/{} on the SPM", getImageGroupId(), getImageId());
            }

            if (!Guid.isNullOrEmpty(getVdsId())) {
                // Refresh image on the host running the VM
                log.debug("Refreshing image {}/{} on its running host {}", getImageGroupId(),
                        getImageId(), getVdsId());
                try {
                    runInternalAction(ActionType.RefreshVolume, createRefreshVolumeParameters());
                } catch (EngineException e) {
                    log.error("Failed to refresh image {}/{} on its running host {}", getImageGroupId(),
                            getImageId(), getVdsId());
                }
            }
        }

        // We mark the action as succeeded, even if it failed, in order not to fail the live merge operation.
        setSucceeded(true);
        setCommandStatus(CommandStatus.SUCCEEDED);
        return super.endAction();
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.reduceImage;
    }

    private boolean isActiveImage() {
        DiskImage activeDiskImage = diskImageDao.getAllSnapshotsForImageGroup(getImageGroupId())
                .stream()
                .filter(DiskImage::getActive)
                .findFirst().orElse(null);
        return activeDiskImage != null && activeDiskImage.getImageId().equals(getImageId());
    }

    private ReduceImageVDSCommandParameters createReduceImageVDSCommandParameters() {
        return new ReduceImageVDSCommandParameters(getStoragePoolId(),
                getStorageDomainId(),
                getImageGroupId(),
                getImageId(),
                true);
    }

    private RefreshVolumeParameters createRefreshVolumeParameters() {
        RefreshVolumeParameters parameters = new RefreshVolumeParameters(getVdsId(),
                getStoragePoolId(),
                getStorageDomainId(),
                getImageGroupId(),
                getImageId());
        parameters.setParentParameters(getParameters());
        return parameters;
    }

    private void prepareImage() {
        imagesHandler.prepareImage(getStoragePoolId(),
                getStorageDomainId(),
                getImageGroupId(),
                getImageId(),
                getStoragePool().getSpmVdsId());
    }

    private void teardownImage() {
        imagesHandler.teardownImage(getStoragePoolId(),
                getStorageDomainId(),
                getImageGroupId(),
                getImageId(),
                getStoragePool().getSpmVdsId());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getImageGroupId(),
                VdcObjectType.Disk,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!isExecutedAsChildCommand()) {
            return Collections.singletonMap(getImageGroupId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK,
                            EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED));
        }
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (!isExecutedAsChildCommand() && getVm() != null) {
            return Collections.singletonMap(getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM,
                            EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED));
        }
        return null;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addAuditLogCustomValues();
        return getSucceeded() ?
                AuditLogType.USER_REDUCE_DISK_FINISHED_SUCCESS : AuditLogType.USER_REDUCE_DISK_FINISHED_FAILURE;
    }

    private void addAuditLogCustomValues() {
        this.addCustomValue("DiskAlias", getDiskImage().getDiskAlias());
    }
}
