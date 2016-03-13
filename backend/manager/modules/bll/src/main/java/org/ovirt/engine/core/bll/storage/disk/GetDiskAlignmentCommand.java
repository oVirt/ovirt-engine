package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.GetDiskAlignmentParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskAlignment;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetDiskAlignmentVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetDiskImageAlignmentVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetDiskLunAlignmentVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class GetDiskAlignmentCommand<T extends GetDiskAlignmentParameters> extends CommandBase<T> {
    private Disk diskToScan;
    private Guid vdsInPool;
    private Guid storagePoolId;
    private VM diskVm;
    private List<PermissionSubject> permsList;

    public GetDiskAlignmentCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getDisk() != null && isImageExclusiveLockNeeded()) {
            return Collections.singletonMap(getDisk().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskIsUsedByGetAlignment()));
        }
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (getDisk() != null && !isImageExclusiveLockNeeded()) {
            return Collections.singletonMap(getDisk().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskIsUsedByGetAlignment()));
        }
        return null;
    }

    private String getDiskIsUsedByGetAlignment() {
        return new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_USED_BY_GET_ALIGNMENT.name())
                .append(String.format("$DiskAlias %1$s", getDiskAlias()))
                .toString();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SCAN_ALIGNMENT);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("diskalias", getDiskAlias());
        }
        return jobProperties;
    }

    @Override
    protected boolean validate() {
        if (getDisk() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }

        if (getVm() == null) {
            addValidationMessageVariable("diskAliases", getDiskAlias());
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK);
        }

        if (getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(Collections.singletonList((DiskImage) getDisk()));
            if (!validate(diskImagesValidator.diskImagesNotLocked()) ||
                    !validate(diskImagesValidator.diskImagesNotIllegal())) {
                return false;
            }

            StorageDomainStatic sds = getStorageDomainStaticDao().get(((DiskImage) getDisk()).getStorageIds().get(0));
            if (!sds.getStorageType().isBlockDomain()) {
                addValidationMessageVariable("diskAlias", getDiskAlias());
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_ALIGNMENT_SCAN_STORAGE_TYPE);
            }
        }

        if (getVm().isRunningOrPaused()) {
            return failValidation(EngineMessage.ERROR_CANNOT_RUN_ALIGNMENT_SCAN_VM_IS_RUNNING);
        }

        if (getVdsIdInGroup() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
        }

        StoragePool sp = getStoragePoolDao().get(getStoragePoolId());
        if (!validate(new StoragePoolValidator(sp).isUp())) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        GetDiskAlignmentVDSCommandParameters parameters;

        auditLogDirector.log(this, AuditLogType.DISK_ALIGNMENT_SCAN_START);

        acquireExclusiveDiskDbLocks();

        // Live scan is not supported yet, this might become: getVm().getId()
        Guid vmId = Guid.Empty;

        if (getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) getDisk();

            GetDiskImageAlignmentVDSCommandParameters imageParameters =
                    new GetDiskImageAlignmentVDSCommandParameters(getVdsIdInGroup(), vmId);

            imageParameters.setPoolId(getStoragePoolId());
            imageParameters.setDomainId(diskImage.getStorageIds().get(0));
            imageParameters.setImageGroupId(diskImage.getId());
            imageParameters.setImageId(diskImage.getImageId());

            parameters = imageParameters;
        } else if (getDiskStorageType() == DiskStorageType.LUN) {
            LunDisk lunDisk = (LunDisk) getDisk();

            GetDiskLunAlignmentVDSCommandParameters lunParameters =
                    new GetDiskLunAlignmentVDSCommandParameters(getVdsIdInGroup(), vmId);

            lunParameters.setLunId(lunDisk.getLun().getLUNId());

            parameters = lunParameters;
        } else {
            throw new EngineException(EngineError.ENGINE, "Unknown DiskStorageType: " +
                getDiskStorageType().toString() + " Disk id: " + getDisk().getId().toString());
        }

        Boolean isDiskAligned = (Boolean) runVdsCommand(
                VDSCommandType.GetDiskAlignment, parameters).getReturnValue();

        getDisk().setAlignment(isDiskAligned ? DiskAlignment.Aligned : DiskAlignment.Misaligned);
        getDisk().setLastAlignmentScan(new Date());

        getBaseDiskDao().update(getDisk());
        setSucceeded(true);

        releaseExclusiveDiskDbLocks();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permsList == null && getDisk() != null) {
            permsList = new ArrayList<>();
            permsList.add(new PermissionSubject(getDisk().getId(),
                    VdcObjectType.Disk, ActionGroup.ATTACH_DISK));
            permsList.add(new PermissionSubject(getDisk().getId(),
                    VdcObjectType.Disk, ActionGroup.EDIT_DISK_PROPERTIES));
        }
        return permsList;
    }

    protected void acquireExclusiveDiskDbLocks() {
        if (isImageExclusiveLockNeeded()) {
            final DiskImage diskImage = (DiskImage) getDisk();

            TransactionSupport.executeInNewTransaction(() -> {
                getCompensationContext().snapshotEntityStatus(diskImage.getImage());
                getCompensationContext().stateChanged();
                diskImage.setImageStatus(ImageStatus.LOCKED);
                ImagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.LOCKED);
                return null;
            });
        }
    }

    protected void releaseExclusiveDiskDbLocks() {
        if (isImageExclusiveLockNeeded()) {
            final DiskImage diskImage = (DiskImage) getDisk();

            diskImage.setImageStatus(ImageStatus.OK);
            ImagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.OK);
        }
    }

    protected boolean isImageExclusiveLockNeeded() {
        /* In case the volume format is RAW (same as a direct LUN) the exclusive image
         * lock is not needed since the alignment scan can run without any interference
         * by a concurrent running VM.
         */
        return getDiskStorageType() == DiskStorageType.IMAGE &&
                ((DiskImage) getDisk()).getVolumeFormat() != VolumeFormat.RAW;
    }

    @Override
    public Guid getStoragePoolId() {
        if (storagePoolId == null && getVm() != null) {
            storagePoolId = getVm().getStoragePoolId();
        }
        return storagePoolId;
    }

    protected Guid getVdsIdInGroup() {
        if (vdsInPool == null && getCluster() != null) {
            List<VDS> vdsInPoolList = getVdsDao().getAllForClusterWithStatus(getCluster().getId(), VDSStatus.Up);
            if (!vdsInPoolList.isEmpty()) {
                vdsInPool = vdsInPoolList.get(0).getId();
            }
        }
        return vdsInPool;
    }

    @Override
    public VM getVm() {
        if (diskVm == null && getDisk() != null) {
            diskVm = getVmDao().getVmsListForDisk(getDisk().getId(), false).stream().findFirst().orElse(null);
        }
        return diskVm;
    }

    public String getDiskAlias() {
        if (getDisk() != null) {
            return getDisk().getDiskAlias();
        }
        return "";
    }

    protected DiskStorageType getDiskStorageType() {
        return getDisk().getDiskStorageType();
    }

    protected Disk getDisk() {
        if (diskToScan == null) {
            diskToScan = getDiskDao().get(getParameters().getDiskId());
        }
        return diskToScan;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.DISK_ALIGNMENT_SCAN_SUCCESS : AuditLogType.DISK_ALIGNMENT_SCAN_FAILURE;
    }
}
