package org.ovirt.engine.core.bll.storage.disk;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.StorageJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SparsifyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class SparsifyImageCommand<T extends StorageJobCommandParameters> extends StorageJobCommand<T> {

    private List<Pair<VM, VmDevice>> vmsForDisk;
    private DiskImage diskImage;

    @Inject
    private VmDao vmDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private ImagesHandler imagesHandler;

    public SparsifyImageCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public SparsifyImageCommand(Guid commandId) {
        super(commandId);
    }

    private DiskImage getDiskImage() {
        if (diskImage == null) {
            diskImage = diskImageDao.get(getParameters().getImageId());
        }
        return diskImage;
    }

    private List<Pair<VM, VmDevice>> getVmsForDisk() {
        if (vmsForDisk == null && getDiskImage() != null) {
            vmsForDisk = vmDao.getVmsWithPlugInfo(getDiskImage().getId());
        }
        return vmsForDisk;
    }

    @Override
    protected void init() {
        super.init();
        if (getDiskImage() != null && getDiskImage().getVmEntityType() != null
                && getDiskImage().getVmEntityType().isTemplateType()) {
            initVmTemplateId();
        }
    }

    private void initVmTemplateId() {
        Map<Boolean, VmTemplate> templateMap = vmTemplateDao.getAllForImage(getDiskImage().getImageId());

        if (!templateMap.isEmpty()) {
            setVmTemplateId(templateMap.values().iterator().next().getId());
        }
    }

    @Override
    protected boolean validate() {
        DiskValidator diskValidator = new DiskValidator(getDiskImage());
        if (!validate(diskValidator.isDiskExists()) ||
                !validate(diskValidator.isDiskPluggedToAnyNonDownVm(false)) ||
                !validate(diskValidator.isSparsifySupported())) {
            return false;
        }

        if (diskImageDao.getAllSnapshotsForImageGroup(getDiskImage().getId()).size() > 1) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_HAS_SNAPSHOTS);
        }

        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(Collections.singletonList(getDiskImage()));
        return validate(diskImagesValidator.diskImagesNotIllegal()) &&
                validate(diskImagesValidator.diskImagesNotLocked()) &&
                validate(diskImagesValidator.diskImagesHaveNoDerivedDisks(null));
    }

    @Override
    protected void executeCommand() {
        lockImageInDb();
        VDSReturnValue vdsReturnValue = vdsCommandsHelper.runVdsCommandWithFailover(
                VDSCommandType.SparsifyImage,
                new SparsifyImageVDSCommandParameters(
                        getParameters().getStorageJobId(),
                        getDiskImage().getStorageIds().get(0),
                        getDiskImage().getId(),
                        getDiskImage().getImageId()),
                getDiskImage().getStoragePoolId(), this);
        if (!vdsReturnValue.getSucceeded()) {
            setCommandStatus(CommandStatus.FAILED);
            unlockImageInDb();
        }
        setSucceeded(vdsReturnValue.getSucceeded());
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
            diskImage.setImageStatus(ImageStatus.LOCKED);
            imagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.LOCKED);
            getCompensationContext().stateChanged();
            return null;
        });
    }

    private void unlockImageInDb() {
        DiskImage diskImage = getDiskImage();
        diskImage.setImageStatus(ImageStatus.OK);
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
        if (getDiskImage() == null || getDiskImage().getVmEntityType() == null) {
            return null;
        }

        if (getDiskImage().getVmEntityType().isVmType()) {
            return getSharedLocksForVmDisk();
        }

        if (getDiskImage().getVmEntityType().isTemplateType()) {
            return getSharedLocksForTemplateDisk();
        }

        log.warn("No shared locks are taken while sparsifying disk of entity: {}", getDiskImage().getVmEntityType());
        return null;
    }

    private Map<String, Pair<String, String>> getSharedLocksForVmDisk() {
        Map<String, Pair<String, String>> sharedLocks = new HashMap<>();

        if (getVmsForDisk() != null) {
            for (Pair<VM, VmDevice> vmForDisk : getVmsForDisk()) {
                sharedLocks.put(vmForDisk.getFirst().getId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED));
            }
        }

        return sharedLocks;
    }

    private Map<String, Pair<String, String>> getSharedLocksForTemplateDisk() {
        return Collections.singletonMap(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, EngineMessage.VM_TEMPLATE_IS_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SPARSIFY);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("DiskAlias", getDiskImage().getDiskAlias());

        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_SPARSIFY_IMAGE_START
                    : AuditLogType.USER_SPARSIFY_IMAGE_FINISH_FAILURE;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_SPARSIFY_IMAGE_FINISH_SUCCESS
                    : AuditLogType.USER_SPARSIFY_IMAGE_FINISH_FAILURE;

        default:
            return AuditLogType.USER_SPARSIFY_IMAGE_FINISH_FAILURE;
        }
    }

}
