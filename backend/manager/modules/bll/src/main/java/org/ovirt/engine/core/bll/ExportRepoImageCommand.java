package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.provider.OpenStackImageProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.UploadImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@LockIdNameAttribute
public class ExportRepoImageCommand<T extends ExportRepoImageParameters> extends CommandBase<T> {

    private DiskImage diskImage;

    private OpenStackImageProviderProxy providerProxy;

    public ExportRepoImageCommand(T parameters) {
        super(parameters);
        getParameters().setCommandType(getActionType());
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
        return Collections.singletonMap(getDiskImage().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskIsBeingExportedMessage()));
    }

    private String getDiskIsBeingExportedMessage() {
        StringBuilder builder = new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_BEING_EXPORTED.name());
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

        Guid taskId = getAsyncTaskId();
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
                            getParameters().getImageGroupID()));
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
        permissionSubjects.add(new PermissionSubject(getDiskImage().getId(),
                VdcObjectType.Disk, ActionGroup.ATTACH_DISK));
        permissionSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage, ActionGroup.CREATE_DISK));
        return permissionSubjects;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EXPORT);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
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
    public Guid getStorageDomainId() {
        return getDiskImage() != null ? getDiskImage().getStorageIds().get(0) : null;
    }

    @Override
    public Guid getStoragePoolId() {
        return getDiskImage() != null ? getDiskImage().getStoragePoolId() : null;
    }

    protected DiskDao getDiskDao() {
        return getDbFacade().getDiskDao();
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

    @Override
    protected boolean canDoAction() {
        if (getDiskImage() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }

        if (!validate(new StorageDomainValidator(getStorageDomain()).isDomainExistAndActive())) {
            return false;
        }

        // At the moment it's not possible to export images that have a snapshot
        // or that are based on a a template.
        if (!getDiskImage().getParentId().equals(Guid.Empty)) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED);
        }

        for (VM vm : getVmDAO().getVmsListForDisk(getDiskImage().getId())) {
            if (vm.getStatus() != VMStatus.Down) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
            }
        }

        return true;
    }

}
