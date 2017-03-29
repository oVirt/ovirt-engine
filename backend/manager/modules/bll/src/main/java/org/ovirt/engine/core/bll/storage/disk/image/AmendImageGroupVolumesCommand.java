package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AmendImageGroupVolumesCommandParameters;
import org.ovirt.engine.core.common.action.AmendVolumeCommandParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AmendImageGroupVolumesCommand<T extends AmendImageGroupVolumesCommandParameters>
        extends CommandBase<T> implements SerialChildExecutingCommand {

    private DiskImage diskImage;
    private List<Pair<VM, VmDevice>> vmsForDisk = new ArrayList<>();

    @Inject
    private ImagesHandler imagesHandler;

    public AmendImageGroupVolumesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private DiskImage getDiskImage() {
        if (diskImage == null) {
            diskImage = (DiskImage) diskDao.get(getParameters().getImageGroupID());
        }
        return diskImage;
    }

    @Override
    protected boolean validate() {
        DiskValidator diskValidator = new DiskValidator(getDiskImage());
        if (!validate(diskValidator.isDiskExists()) &&
                !validate(diskValidator.isDiskPluggedToVmsThatAreNotDown(false, vmsForDisk))) {
            return false;
        }
        setStoragePoolId(getDiskImage().getStoragePoolId());
        if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }
        if (!FeatureSupported.qcowCompatSupported(getStoragePool().getCompatibilityVersion())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_AMEND_NOT_SUPPORTED_BY_DC_VERSION,
                    String.format("$dataCenterVersion %s", getStoragePool().getCompatibilityVersion().toString()));
        }
        if (getDiskImage().getVmEntityType().isTemplateType()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANT_AMEND_TEMPLATE_DISK);
        }
        setStorageDomainId(getDiskImage().getStorageIds().get(0));
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(getStorageDomain());
        if (!validate(storageDomainValidator.isDomainExistAndActive())) {
            return false;
        }
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(Collections.singletonList(getDiskImage()));
        return validate(diskImagesValidator.diskImagesNotIllegal()) &&
                validate(diskImagesValidator.diskImagesNotLocked());
    }

    @Override
    protected void executeCommand() {
        lockImageInDb();
        List<DiskImage> images = diskImageDao
                .getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
        getParameters().setImageIds(ImagesHandler.getDiskImageIds(images.stream()
                .filter(disk -> disk.isQcowFormat())
                .collect(Collectors.toList())));
        persistCommand(getActionType(), getCallback() != null);
        setSucceeded(true);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
        }
        jobProperties.put("action", "Amending");
        jobProperties.put("diskalias", getDiskImage().getDiskAlias());
        return jobProperties;
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
                completedChildren + 1, getParameters().getImageIds().size(), imageId);

        amendVolume(imageId);
        return true;
    }

    private void amendVolume(Guid imageId) {
        AmendVolumeCommandParameters parameters =
                new AmendVolumeCommandParameters(getDiskImage().getStoragePoolId(),
                        buildImageLocationInfo(getDiskImage().getStorageIds().get(0), getDiskImage().getId(), imageId),
                        getParameters().getQcowCompat());

        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setJobWeight(getParameters().getOperationsJobWeight().get(imageId.toString()));
        runInternalActionWithTasksContext(VdcActionType.AmendVolume, parameters);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        DiskImage diskImage = getDiskImage();
        if (diskImage == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(
                new PermissionSubject(diskImage.getId(),
                        VdcObjectType.Disk,
                        getActionType().getActionGroup()));
    }

    private void lockImageInDb() {
        final DiskImage diskImage = getDiskImage();

        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntityStatus(diskImage.getImage());
            imagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.LOCKED);
            getCompensationContext().stateChanged();
            return null;
        });
    }

    private void unlockImageInDb() {
        DiskImage diskImage = getDiskImage();
        imagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.OK);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return getDiskImage() != null ? Collections.singletonMap(getDiskImage().getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED))
                : null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (getDiskImage() == null) {
            return null;
        }

        if (getDiskImage().getVmEntityType().isVmType()) {
            return getSharedLocksForVmDisk();
        }

        log.warn("No shared locks are taken while amending disk of entity: {}", getDiskImage().getVmEntityType());
        return null;
    }

    private Map<String, Pair<String, String>> getSharedLocksForVmDisk() {
        Map<String, Pair<String, String>> lockMap = new HashMap<>();
        if (getDiskImage() != null) {
            List<Pair<VM, VmDevice>> vmsForDisk = vmDao.getVmsWithPlugInfo(getDiskImage().getId());
            if (!vmsForDisk.isEmpty()) {
                for (Pair<VM, VmDevice> pair : vmsForDisk) {
                    lockMap.put(pair.getFirst().getId().toString(),
                            LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM,
                                    EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED));
                }
            }
        }
        return lockMap;
    }

    @Override
    protected void endSuccessfully() {
        super.endSuccessfully();
        unlockImageInDb();
    }

    @Override
    protected void endWithFailure() {
        super.endWithFailure();
        unlockImageInDb();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__AMEND);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("DiskAlias", getDiskImage().getDiskAlias());

        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_AMEND_IMAGE_START
                    : AuditLogType.USER_AMEND_IMAGE_FINISH_FAILURE;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_AMEND_IMAGE_FINISH_SUCCESS
                    : AuditLogType.USER_AMEND_IMAGE_FINISH_FAILURE;

        default:
            return AuditLogType.USER_AMEND_IMAGE_FINISH_FAILURE;
        }
    }

    private VdsmImageLocationInfo buildImageLocationInfo(Guid domId, Guid imageGroupId, Guid imageId) {
        return new VdsmImageLocationInfo(domId, imageGroupId, imageId, null);
    }
}
