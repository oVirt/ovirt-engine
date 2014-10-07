package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExtendImageSizeParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ExtendImageSizeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ExtendVmDiskSizeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class ExtendImageSizeCommand<T extends ExtendImageSizeParameters> extends BaseImagesCommand<T>
        implements QuotaStorageDependent {

    private List<PermissionSubject> permissionsList;
    private List<VM> vmsDiskPluggedTo;

    public ExtendImageSizeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue vdsReturnValue = extendUnderlyingVolumeSize(getImage());
        setSucceeded(vdsReturnValue.getSucceeded());

        if (vdsReturnValue.getSucceeded()) {
            Guid taskId = createTask(getAsyncTaskId(),
                    vdsReturnValue.getCreationInfo(), getParameters().getParentCommand());
            getReturnValue().getInternalVdsmTaskIdList().add(taskId);
        } else {
            updateAuditLog(AuditLogType.USER_EXTEND_DISK_SIZE_FAILURE, getParameters().getNewSizeInGB());
        }
    }

    private VDSReturnValue extendUnderlyingVolumeSize(DiskImage diskImage) {
        ExtendImageSizeVDSCommandParameters params = new ExtendImageSizeVDSCommandParameters(
                diskImage.getStoragePoolId(),
                diskImage.getStorageIds().get(0),
                diskImage.getId(),
                diskImage.getImageId(),
                getParameters().getNewSize()
        );

        return runVdsCommand(VDSCommandType.ExtendImageSize, params);
    }

    @Override
    protected void endSuccessfully() {
        updateRelevantVms();

        DiskImage diskImage = getImageInfo();
        if (diskImage != null && getImage().getSize() != diskImage.getSize()) {
            getReturnValue().setActionReturnValue(diskImage.getSize());
            getImageDao().updateImageSize(diskImage.getImageId(), diskImage.getSize());
            updateAuditLog(AuditLogType.USER_EXTEND_DISK_SIZE_SUCCESS, diskImage.getSizeInGigabytes());
        }

        setSucceeded(true);
    }

    private void updateRelevantVms() {
        List<VM> vms = getVmsDiskPluggedTo();

        for (VM vm : vms) {
            try {
                VDSReturnValue ret = extendVmDiskSize(vm, getParameters().getNewSize());
                if (!ret.getSucceeded()) {
                    updateAuditLogFailedToUpdateVM(vm.getName());
                }
            } catch (VdcBLLException e) {
                log.warn("Failed to update VM '{}' with the new volume size due to error, "
                                + "VM should be restarted to detect the new size: {}",
                        vm.getName(),
                        e.getMessage());
                log.debug("Exception", e);
                updateAuditLogFailedToUpdateVM(vm.getName());
            }
        }
    }

    private VDSReturnValue extendVmDiskSize(VM vm, Long newSize) {
        Guid vdsId, vmId;

        if (vm.getStatus().isDownOrSuspended()) {
            vdsId = getStoragePool().getSpmVdsId();
            vmId = Guid.Empty;
        } else {
            vdsId = vm.getRunOnVds();
            vmId = vm.getId();
        }

        ExtendVmDiskSizeVDSCommandParameters params = new ExtendVmDiskSizeVDSCommandParameters(vdsId, vmId,
                getParameters().getStoragePoolId(), getParameters().getStorageDomainId(),
                getParameters().getImageId(), getParameters().getImageGroupID(), newSize);

        return runVdsCommand(VDSCommandType.ExtendVmDiskSize, params);
    }

    private DiskImage getImageInfo() {
        DiskImage diskImage = null;
        GetImageInfoVDSCommandParameters params = new GetImageInfoVDSCommandParameters(
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupID(),
                getParameters().getImageId()
        );

        try {
            diskImage = (DiskImage) runVdsCommand(VDSCommandType.GetImageInfo, params).getReturnValue();
        } catch (VdcBLLException e) {
            log.error("Failed to retrieve image '{}' info: {}",
                    params.getImageId(),
                    e.getMessage());
            log.debug("Exception", e);
        }
        return diskImage;
    }

    @Override
    protected void endWithFailure() {
        getReturnValue().setEndActionTryAgain(false);

        DiskImage diskImage = getImageInfo();
        if (diskImage != null && getImage().getSize() != diskImage.getSize()) {
            getReturnValue().setActionReturnValue(diskImage.getSize());
            getImageDao().updateImageSize(diskImage.getImageId(), diskImage.getSize());
        }

        updateAuditLog(AuditLogType.USER_EXTEND_DISK_SIZE_FAILURE, getParameters().getNewSizeInGB());
    }

    private void updateAuditLog(AuditLogType auditLogType, Long imageSizeInGigabytes) {
        addCustomValue("DiskAlias", getImage().getDiskAlias());
        addCustomValue("NewSize", String.valueOf(imageSizeInGigabytes));
        auditLogDirector.log(this, auditLogType);
    }

    private void updateAuditLogFailedToUpdateVM(String vmName) {
        addCustomValue("VmName", vmName);
        auditLogDirector.log(this, AuditLogType.USER_EXTEND_DISK_SIZE_UPDATE_VM_FAILURE);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permissionsList == null) {
            permissionsList = new ArrayList<PermissionSubject>();
            permissionsList.add(new PermissionSubject(getImage().getId(),
                    VdcObjectType.Disk, ActionGroup.EDIT_DISK_PROPERTIES));
        }

        return permissionsList;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> params = new ArrayList<QuotaConsumptionParameter>();
        if (getImage() != null) {
            double newSizeInGigabytes = Long.valueOf(getParameters().getNewSize() / BYTES_IN_GB).doubleValue();
            double currentSizeInGigabytes = Long.valueOf(getImage().getSizeInGigabytes()).doubleValue();
            double additionalDiskSpace = newSizeInGigabytes - currentSizeInGigabytes;

            params.add(new QuotaStorageConsumptionParameter(
                    getImage().getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    getParameters().getStorageDomainId(),
                    additionalDiskSpace));
        }
        return params;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EXTEND_IMAGE_SIZE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.extendImageSize;
    }

    private List<VM> getVmsDiskPluggedTo() {
        if (vmsDiskPluggedTo == null) {
            List<Pair<VM, VmDevice>> attachedVmsInfo = getVmDAO().getVmsWithPlugInfo(getImage().getId());
            vmsDiskPluggedTo = new LinkedList<>();

            for (Pair<VM, VmDevice> pair : attachedVmsInfo) {
                if (Boolean.TRUE.equals(pair.getSecond().getIsPlugged()) && pair.getSecond().getSnapshotId() == null) {
                   vmsDiskPluggedTo.add(pair.getFirst());
                }
            }
        }
        return vmsDiskPluggedTo;
    }
}
