package org.ovirt.engine.core.bll.storage.repoimage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.UploadImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@SuppressWarnings("unused")
public class ExportRepoImageCommand<T extends ExportRepoImageParameters> extends CommandBase<T> {

    private DiskImage diskImage;

    private OpenStackImageProviderProxy providerProxy;

    public ExportRepoImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        getParameters().setCommandType(getActionType());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    protected ProviderProxyFactory getProviderProxyFactory() {
        return ProviderProxyFactory.getInstance();
    }

    protected OpenStackImageProviderProxy getProviderProxy() {
        if (providerProxy == null) {
            providerProxy = OpenStackImageProviderProxy
                    .getFromStorageDomainId(getParameters().getDestinationDomainId());
        }
        return providerProxy;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        DiskImage diskImage = getDiskImage();
        if (diskImage == null) {
            return null;
        }
        return Collections.singletonMap(diskImage.getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskIsBeingExportedMessage()));
    }

    private String getDiskIsBeingExportedMessage() {
        StringBuilder builder = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_BEING_EXPORTED.name());
        if (getDiskImage() != null) {
            builder.append(String.format("$DiskAlias %1$s", getDiskImage().getDiskAlias()));
        }
        return builder.toString();
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.copyImage;
    }

    protected void acquireImageDbLock() {
        getDiskImage().setImageStatus(ImageStatus.LOCKED);
        ImagesHandler.updateImageStatus(getDiskImage().getImageId(), getDiskImage().getImageStatus());
    }

    protected void releaseImageDbLock() {
        getDiskImage().setImageStatus(ImageStatus.OK);
        ImagesHandler.updateImageStatus(getDiskImage().getImageId(), getDiskImage().getImageStatus());
    }

    @Override
    protected void executeCommand() {
        DiskImage diskImage = getDiskImage();
        OpenStackImageProviderProxy proxy = getProviderProxy();

        acquireImageDbLock();

        String newImageId = proxy.createImageFromDiskImage(diskImage);
        getParameters().setParentCommand(VdcActionType.ExportRepoImage);

        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getImageGroupID()));

        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.UploadImage,
                new UploadImageVDSCommandParameters(
                        getStorageDomain().getStoragePoolId(),
                        getStorageDomain().getId(),
                        diskImage.getId(),
                        diskImage.getImageId(),
                        new HttpLocationInfo(
                                getProviderProxy().getImageUrl(newImageId), getProviderProxy().getUploadHeaders()
                        )
                ));

        if (vdsReturnValue.getSucceeded()) {
            getReturnValue().getVdsmTaskIdList().add(
                    createTask(taskId,
                            vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Disk,
                            getParameters().getImageGroupID(), getParameters().getDestinationDomainId()));
        }

        getReturnValue().setActionReturnValue(newImageId);
        setSucceeded(true);
    }

    @Override
    public void endSuccessfully() {
        super.endSuccessfully();
        releaseImageDbLock();
    }

    @Override
    public void endWithFailure() {
        super.endWithFailure();
        releaseImageDbLock();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionSubjects = new ArrayList<>();
        DiskImage diskImage = getDiskImage();
        if (diskImage != null) {
            permissionSubjects.add(new PermissionSubject(diskImage.getId(),
                    VdcObjectType.Disk, ActionGroup.ATTACH_DISK));
        }
        permissionSubjects.add(new PermissionSubject(getParameters().getDestinationDomainId(),
                VdcObjectType.Storage, ActionGroup.CREATE_DISK));
        return permissionSubjects;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__EXPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("diskalias", getDiskImage() != null ? getDiskImage().getDiskAlias() : "");
            jobProperties.put("storage", getStorageDomain() != null ? getStorageDomain().getStorageName() : "");
        }
        return jobProperties;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
            case EXECUTE:
                return getSucceeded() ? AuditLogType.USER_EXPORT_IMAGE
                        : AuditLogType.USER_EXPORT_IMAGE_FINISHED_FAILURE;
            case END_SUCCESS:
                return AuditLogType.USER_EXPORT_IMAGE_FINISHED_SUCCESS;
            case END_FAILURE:
                return AuditLogType.USER_EXPORT_IMAGE_FINISHED_FAILURE;
        }
        return AuditLogType.UNASSIGNED;
    }

    @Override
    public Guid getStorageDomainId() {
        return getDiskImage() != null ? getDiskImage().getStorageIds().get(0) : null;
    }

    @Override
    public Guid getStoragePoolId() {
        return getDiskImage() != null ? getDiskImage().getStoragePoolId() : null;
    }

    protected DiskImage getDiskImage() {
        if (diskImage == null) {
            Disk disk = getDiskDao().get(getParameters().getImageGroupID());
            if (disk != null && disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                diskImage = (DiskImage) disk;
            }
        }
        return diskImage;
    }

    private boolean validateDiskImage() {
        Disk disk = getDiskDao().get(getParameters().getImageGroupID());
        if (disk != null && !validate(new DiskValidator(disk).validateUnsupportedDiskStorageType(
                DiskStorageType.LUN, DiskStorageType.CINDER)) ) {
            return false;
        }
        if (getDiskImage() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }
        return true;
    }

    public String getRepoImageName() {
        return getDiskImage() != null ? getDiskImage().getDiskAlias() : "";
    }

    public String getDestinationStorageDomainName() {
        return getStorageDomainDao().get(getParameters().getDestinationDomainId()).getName();
    }

    @Override
    protected boolean validate() {
        if (!validateDiskImage()) {
            return false;
        }

        if (!validate(new StorageDomainValidator(getStorageDomain()).isDomainExistAndActive())) {
            return false;
        }

        // At the moment it's not possible to export images that have a snapshot
        // or that are based on a a template.
        if (!getDiskImage().getParentId().equals(Guid.Empty)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED);
        }

        for (VM vm : getVmDao().getVmsListForDisk(getDiskImage().getId(), false)) {
            if (vm.getStatus() != VMStatus.Down) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_RUNNING);
            }
        }

        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(Arrays.asList(getDiskImage()));
        if (!validate(diskImagesValidator.diskImagesNotIllegal())
                || !validate(diskImagesValidator.diskImagesNotLocked())) {
            return false;
        }

        return true;
    }

}
