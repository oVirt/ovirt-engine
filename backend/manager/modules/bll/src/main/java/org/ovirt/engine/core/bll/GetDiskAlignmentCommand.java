package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.common.action.GetDiskAlignmentParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskAlignment;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.vdscommands.GetDiskAlignmentVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetDiskImageAlignmentVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetDiskLunAlignmentVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;


@LockIdNameAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class GetDiskAlignmentCommand<T extends GetDiskAlignmentParameters> extends CommandBase<T> {
    private static final long serialVersionUID = -7266894047095142486L;

    private Disk diskToScan;
    private Guid vdsInPool, storagePoolId, vdsGroupId;
    private VM diskVm;
    private List<PermissionSubject> permsList;

    public GetDiskAlignmentCommand(T parameters) {
        super(parameters);
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
        return new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_USED_BY_GET_ALIGNMENT.name())
                .append(String.format("DiskAlias %1$s", getDiskAlias()))
                .toString();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__SCAN_ALIGNMENT);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
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
    protected boolean canDoAction() {
        if (getDisk() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }

        if (getDiskType() == DiskStorageType.IMAGE) {
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(Arrays.asList((DiskImage) getDisk()));
            if (!validate(diskImagesValidator.diskImagesNotLocked()) ||
                    !validate(diskImagesValidator.diskImagesNotIllegal())) {
                return false;
            }
        }

        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK);
        }

        if (isImageExclusiveLockNeeded() && getVm().isRunningOrPaused()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
        }

        if (getVdsIdInPool() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
        }

        if (!validate(new StoragePoolValidator(getStoragePoolDao().get(getStoragePoolId())).isUp())) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        GetDiskAlignmentVDSCommandParameters parameters;

        acquireExclusiveDiskDbLocks();

        // Live scan is not supported yet, this might become: getVm().getId()
        Guid vmId = Guid.Empty;

        if (getDiskType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) getDisk();

            GetDiskImageAlignmentVDSCommandParameters imageParameters =
                    new GetDiskImageAlignmentVDSCommandParameters(getVdsIdInPool(), vmId);

            imageParameters.setPoolId(getStoragePoolId());
            imageParameters.setDomainId(diskImage.getStorageIds().get(0));
            imageParameters.setImageGroupId(diskImage.getimage_group_id());
            imageParameters.setImageId(diskImage.getImageId());

            parameters = imageParameters;
        } else if (getDiskType() == DiskStorageType.LUN) {
            LunDisk lunDisk = (LunDisk) getDisk();

            GetDiskLunAlignmentVDSCommandParameters lunParameters =
                    new GetDiskLunAlignmentVDSCommandParameters(getVdsIdInPool(), vmId);

            lunParameters.setLunId(lunDisk.getLun().getLUN_id());

            parameters = lunParameters;
        } else {
            throw new VdcBLLException(VdcBllErrors.ENGINE, "Unknown DiskStorageType: " +
                getDiskType().toString() + " Disk id: " + getDisk().getId().toString());
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
            permsList = new ArrayList<PermissionSubject>();
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
            final ImageStatus imgStatus = diskImage.getImageStatus();

            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    getCompensationContext().snapshotEntityStatus(diskImage.getImage(), imgStatus);
                    getCompensationContext().stateChanged();
                    diskImage.setImageStatus(ImageStatus.LOCKED);
                    ImagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.LOCKED);
                    return null;
                }});
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
        return (getDiskType() == DiskStorageType.IMAGE &&
                ((DiskImage) getDisk()).getVolumeFormat() != VolumeFormat.RAW);
    }

    public Guid getStoragePoolId() {
        if (storagePoolId == null && getVm() != null) {
            storagePoolId = getVm().getStoragePoolId();
        }
        return storagePoolId;
    }

    public Guid getVdsGroupId() {
        if (vdsGroupId == null && getVm() != null) {
            vdsGroupId = getVm().getVdsGroupId();
        }
        return vdsGroupId;
    }

    protected Guid getVdsIdInPool() {
        if (vdsInPool == null && getVdsGroupId() != null) {
            List<VDS> vdsInPoolList = getVdsDAO().getAllForVdsGroupWithStatus(getVdsGroupId(), VDSStatus.Up);
            if (!vdsInPoolList.isEmpty()) {
                vdsInPool = vdsInPoolList.get(0).getId();
            }
        }
        return vdsInPool;
    }

    protected DiskDao getDiskDao() {
        return getDbFacade().getDiskDao();
    }

    protected BaseDiskDao getBaseDiskDao() {
        return getDbFacade().getBaseDiskDao();
    }

    protected StorageDomainDAO getStorageDomainDao() {
        return getDbFacade().getStorageDomainDao();
    }

    protected StoragePoolDAO getStoragePoolDao() {
        return getDbFacade().getStoragePoolDao();
    }

    public VM getVm() {
        if (diskVm == null && getDisk() != null) {
            for (VM vm : getVmDAO().getVmsListForDisk(getDisk().getId())) {
                diskVm = vm;
                break;
            }
        }
        return diskVm;
    }

    public String getDiskAlias() {
        if (getDisk() != null) {
            return getDisk().getDiskAlias();
        }
        return "";
    }

    protected DiskStorageType getDiskType() {
        return getDisk().getDiskStorageType();
    }

    protected Disk getDisk() {
        if (diskToScan == null) {
            diskToScan = getDiskDao().get((Guid) getParameters().getDiskId());
        }
        return diskToScan;
    }
}
