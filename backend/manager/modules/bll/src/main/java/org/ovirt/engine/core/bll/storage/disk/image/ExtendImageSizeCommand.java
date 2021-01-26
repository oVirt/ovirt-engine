package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExtendImageSizeParameters;
import org.ovirt.engine.core.common.action.RefreshVolumeParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ExtendImageSizeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ExtendVmDiskSizeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.VmDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class ExtendImageSizeCommand<T extends ExtendImageSizeParameters> extends BaseImagesCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ImageDao imageDao;
    @Inject
    private VmDao vmDao;

    private List<PermissionSubject> permissionsList;
    private List<VM> vmsDiskPluggedTo;

    public ExtendImageSizeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        // This method extends size for RAW disks only (using volume.extendSize() on VDSM)
        VDSReturnValue vdsReturnValue = extendUnderlyingVolumeSize(getImage());
        setSucceeded(vdsReturnValue.getSucceeded());

        if (vdsReturnValue.getSucceeded()) {
            Guid taskId = createTask(getAsyncTaskId(),
                    vdsReturnValue.getCreationInfo(), getParameters().getParentCommand());
            getTaskIdList().add(taskId);
            if (getParameters().getParentNotifiesCallback()) {
                getReturnValue().getVdsmTaskIdList().add(taskId);
                getReturnValue().getInternalVdsmTaskIdList().add(taskId);
                getParameters().setVdsmTaskIds(new ArrayList<>(Arrays.asList(taskId)));
                persistCommand(getParameters().getParentCommand(), true);
            }
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
        boolean updateAllVmsSucceeded = true;
        if (getImage().getActive()) {
            // This method extends size for QCOW disks only (using hsm.updateVolumeSize() on VDSM)
            updateAllVmsSucceeded = updateRelevantVms();
        } else if (getImage().hasRawBlock()) {
            refreshVolume();
        }

        DiskImage diskImage = getImageInfo();
        if (diskImage != null && getImage().getSize() != diskImage.getSize()) {
            getReturnValue().setActionReturnValue(diskImage.getSize());
            imageDao.updateImageSize(diskImage.getImageId(), diskImage.getSize());
            setDestinationImageId(diskImage.getImageId());
            completeImageData(diskImage);
            updateAuditLog(AuditLogType.USER_EXTEND_DISK_SIZE_SUCCESS, diskImage.getSizeInGigabytes());
        }

        setSucceeded(updateAllVmsSucceeded);
    }

    private boolean updateRelevantVms() {
        List<VM> vms = getVmsDiskPluggedTo();
        if (vms.isEmpty()) {
            return extendFloatingDiskSize();
        }
        boolean updateAllVmsSucceeded = true;
        for (VM vm : vms) {
            try {
                VDSReturnValue ret = extendVmDiskSize(vm, getParameters().getNewSize());
                if (!ret.getSucceeded()) {
                    updateAuditLogFailedToUpdateVM(vm.getName());
                    updateAllVmsSucceeded = false;
                }
            } catch (EngineException e) {
                log.warn("Failed to update VM '{}' with the new volume size due to error, "
                                + "VM should be restarted to detect the new size: {}",
                        vm.getName(),
                        e.getMessage());
                log.debug("Exception", e);
                updateAuditLogFailedToUpdateVM(vm.getName());
                updateAllVmsSucceeded = false;
            }
        }
        return updateAllVmsSucceeded;
    }

    private boolean extendFloatingDiskSize() {
        try {
            VDSReturnValue ret = extendVmDiskSize(null, getParameters().getNewSize());
            if (!ret.getSucceeded()) {
                updateAuditLog(AuditLogType.USER_EXTEND_DISK_SIZE_FAILURE, getParameters().getNewSizeInGB());
                return false;
            }
        } catch (EngineException e) {
            updateAuditLog(AuditLogType.USER_EXTEND_DISK_SIZE_FAILURE, getParameters().getNewSizeInGB());
            log.debug("Exception", e);
            return false;
        }
        return true;
    }

    private VDSReturnValue extendVmDiskSize(VM vm, Long newSize) {
        Guid vdsId;
        Guid vmId;

        if (vm == null || vm.getStatus().isDownOrSuspended()) {
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

    /**
     * Refreshes the size of an extended volume for the VM having the image to extend attached.
     * Note that because this is not an active layer image, it can only be in use by a single VM.
     */
    private void refreshVolume() {
        List<VM> vms = getVmsDiskPluggedTo();
        if (vms.isEmpty() || vms.get(0).getStatus().isDownOrSuspended()) {
            return;
        }
        VM vm = vms.get(0);

        log.info("Refreshing size of extended volume '{}' for VM '{}' on host '{}'",
                getParameters().getImageId(), vm.getName(), vm.getRunOnVdsName());

        RefreshVolumeParameters parameters = new RefreshVolumeParameters(
                vm.getRunOnVds(),
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupID(),
                getParameters().getImageId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());

        ActionReturnValue returnValue =
                runInternalAction(ActionType.RefreshVolume,
                        parameters,
                        cloneContextAndDetachFromParent());

        if (returnValue == null || !returnValue.getSucceeded()) {
            // TODO this might be better in MergeExtendCommand: there is a race due to the refresh
            // being called from endSuccessfully(), and it would also give more context upon error.
            // The flow to refresh an internal volume is called only from Live Merge, thus the
            // solution to retry the operation.  This should be updated if ever used elsewhere.
            log.warn("Failed to update host '{}' with the new size of volume '{}' due to error. "
                            + "Please try the operation again.",
                    vm.getRunOnVdsName(), getParameters().getImageId());
            updateAuditLogFailedToUpdateHost(vm.getRunOnVdsName());
        }
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
        } catch (EngineException e) {
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
            imageDao.updateImageSize(diskImage.getImageId(), diskImage.getSize());
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

    private void updateAuditLogFailedToUpdateHost(String vdsName) {
        addCustomValue("VdsName", vdsName);
        auditLogDirector.log(this, AuditLogType.USER_EXTEND_DISK_SIZE_UPDATE_HOST_FAILURE);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permissionsList == null) {
            permissionsList = new ArrayList<>();
            permissionsList.add(new PermissionSubject(getImage().getId(),
                    VdcObjectType.Disk, ActionGroup.EDIT_DISK_PROPERTIES));
        }

        return permissionsList;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__EXTEND_IMAGE_SIZE);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.extendImageSize;
    }

    private List<VM> getVmsDiskPluggedTo() {
        if (vmsDiskPluggedTo == null) {
            List<Pair<VM, VmDevice>> attachedVmsInfo = vmDao.getVmsWithPlugInfo(getImage().getId());
            vmsDiskPluggedTo = new LinkedList<>();

            for (Pair<VM, VmDevice> pair : attachedVmsInfo) {
                if (Boolean.TRUE.equals(pair.getSecond().isPlugged()) && pair.getSecond().getSnapshotId() == null) {
                   vmsDiskPluggedTo.add(pair.getFirst());
                }
            }
        }
        return vmsDiskPluggedTo;
    }
}
