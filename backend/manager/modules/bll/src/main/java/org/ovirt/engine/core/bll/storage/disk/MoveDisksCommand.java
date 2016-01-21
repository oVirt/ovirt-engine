package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

public class MoveDisksCommand<T extends MoveDisksParameters> extends CommandBase<T> {

    private List<VdcReturnValueBase> vdcReturnValues = new ArrayList<>();
    private List<MoveDiskParameters> moveDiskParametersList = new ArrayList<>();
    private List<LiveMigrateVmDisksParameters> liveMigrateVmDisksParametersList = new ArrayList<>();
    private Map<Guid, DiskImage> diskMap = new HashMap<>();

    public MoveDisksCommand(Guid commandId) {
        super(commandId);
    }

    public MoveDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        updateParameters();

        if (!moveDiskParametersList.isEmpty()) {
            vdcReturnValues.addAll(Backend.getInstance().runMultipleActions(VdcActionType.MoveOrCopyDisk,
                    getParametersArrayList(moveDiskParametersList), false));
        }

        if (!liveMigrateVmDisksParametersList.isEmpty()) {
            vdcReturnValues.addAll(Backend.getInstance().runMultipleActions(VdcActionType.LiveMigrateVmDisks,
                    getParametersArrayList(liveMigrateVmDisksParametersList), false));
        }

        handleChildReturnValue();
        setSucceeded(true);
    }

    private void handleChildReturnValue() {
        for (VdcReturnValueBase vdcReturnValue : vdcReturnValues) {
            getReturnValue().getValidationMessages().addAll(vdcReturnValue.getValidationMessages());
            getReturnValue().setValid(getReturnValue().isValid() && vdcReturnValue.isValid());
        }
    }

    @Override
    protected boolean validate() {
        if (getParameters().getParametersList().isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED);
        }

        return verifyUnsupportedDisks();
    }

    private boolean verifyUnsupportedDisks() {
        for (MoveDiskParameters moveDiskParameters : getParameters().getParametersList()) {
            Disk disk = getDiskDao().get(moveDiskParameters.getImageGroupID());
            DiskValidator diskValidator = new DiskValidator(disk);
            if (!validate(diskValidator.validateUnsupportedDiskStorageType(
                    DiskStorageType.LUN, DiskStorageType.CINDER))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__MOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    private void addDisksDeactivatedMessage(List<MoveDiskParameters> moveDiskParamsList) {
        setActionMessageParameters();
        addValidationMessageVariable("diskAliases", StringUtils.join(getDisksAliases(moveDiskParamsList), ", "));
        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_MOVE_DISKS_MIXED_PLUGGED_STATUS);
        getReturnValue().setValid(false);
    }

    private void addInvalidVmStateMessage(VM vm){
        setActionMessageParameters();
        addValidationMessageVariable("VmName", vm.getName());
        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP);
        getReturnValue().setValid(false);
    }

    /**
     * For each specified MoveDiskParameters, decide whether it should be moved
     * using offline move or live migrate command.
     */
    protected void updateParameters() {
        Map<VM, List<MoveDiskParameters>> vmDiskParamsMap = createVmDiskParamsMap();

        for (Map.Entry<VM, List<MoveDiskParameters>> vmDiskParamsEntry : vmDiskParamsMap.entrySet()) {
            VM vm = vmDiskParamsEntry.getKey();
            List<MoveDiskParameters> moveDiskParamsList = vmDiskParamsEntry.getValue();

            if (vm == null || vm.isDown() || areAllDisksPluggedToVm(moveDiskParamsList, false)) {
                // Adding parameters for offline move
                moveDiskParametersList.addAll(moveDiskParamsList);
            }
            else if (vm.isRunningAndQualifyForDisksMigration()) {
                if (!areAllDisksPluggedToVm(moveDiskParamsList, true)) {
                    // Cannot live migrate and move disks concurrently
                    addDisksDeactivatedMessage(moveDiskParamsList);
                    continue;
                }

                // Adding parameters for live migrate
                liveMigrateVmDisksParametersList.add(createLiveMigrateVmDisksParameters(moveDiskParamsList, vm.getId()));
            }
            else {
                // Live migrate / move disk is not applicable according to VM status
                addInvalidVmStateMessage(vm);
            }
        }
    }

    /**
     * @return a map of VMs to relevant MoveDiskParameters list.
     */
    private Map<VM, List<MoveDiskParameters>> createVmDiskParamsMap() {
        Map<VM, List<MoveDiskParameters>> vmDisksMap = new HashMap<>();
        for (MoveDiskParameters moveDiskParameters : getParameters().getParametersList()) {
            DiskImage diskImage = getDiskImageDao().get(moveDiskParameters.getImageId());

            Map<Boolean, List<VM>> allVmsForDisk = getVmDao().getForDisk(diskImage.getId(), false);
            List<VM> vmsForPluggedDisk = allVmsForDisk.get(Boolean.TRUE);
            List<VM> vmsForUnpluggedDisk = allVmsForDisk.get(Boolean.FALSE);

            VM vm = vmsForPluggedDisk != null ? vmsForPluggedDisk.get(0) :
                    vmsForUnpluggedDisk != null ? vmsForUnpluggedDisk.get(0) :
                    null; // null is used for floating disks indication

            addDiskToMap(diskImage, vmsForPluggedDisk, vmsForUnpluggedDisk);
            MultiValueMapUtils.addToMap(vm, moveDiskParameters, vmDisksMap);
        }

        return vmDisksMap;
    }

    /**
     * Add the specified diskImage to diskMap; with updated 'plugged' value.
     * (diskMap contains all disks specified in the parameters).
     */
    private void addDiskToMap(DiskImage diskImage, List<VM> vmsForPluggedDisk, List<VM> vmsForUnpluggedDisk) {
        if (vmsForPluggedDisk != null) {
            diskImage.setPlugged(true);
        }
        else if (vmsForUnpluggedDisk != null) {
            diskImage.setPlugged(false);
        }

        diskMap.put(diskImage.getImageId(), diskImage);
    }

    private LiveMigrateDiskParameters createLiveMigrateDiskParameters(MoveDiskParameters moveDiskParameters, Guid vmId) {
        return new LiveMigrateDiskParameters(moveDiskParameters.getImageId(),
                moveDiskParameters.getSourceDomainId(),
                moveDiskParameters.getStorageDomainId(),
                vmId,
                moveDiskParameters.getQuotaId(),
                moveDiskParameters.getDiskProfileId(),
                diskMap.get(moveDiskParameters.getImageId()).getId());
    }

    private LiveMigrateVmDisksParameters createLiveMigrateVmDisksParameters(List<MoveDiskParameters> moveDiskParamsList, Guid vmId) {
        // Create LiveMigrateDiskParameters list
        List<LiveMigrateDiskParameters> liveMigrateDiskParametersList = new ArrayList<>();
        for (MoveDiskParameters moveDiskParameters : moveDiskParamsList) {
            liveMigrateDiskParametersList.add(createLiveMigrateDiskParameters(moveDiskParameters, vmId));
        }

        // Create LiveMigrateVmDisksParameters (multiple disks)
        LiveMigrateVmDisksParameters liveMigrateDisksParameters =
                new LiveMigrateVmDisksParameters(liveMigrateDiskParametersList, vmId);

        return liveMigrateDisksParameters;
    }

    private ArrayList<VdcActionParametersBase> getParametersArrayList(List<? extends VdcActionParametersBase> parametersList) {
        for (VdcActionParametersBase parameters : parametersList) {
            parameters.setSessionId(getParameters().getSessionId());
        }

        return new ArrayList<>(parametersList);
    }

    /**
     * Return true if all specified disks are plugged; otherwise false.
     */
    private boolean areAllDisksPluggedToVm(List<MoveDiskParameters> moveDiskParamsList, boolean plugged) {
        for (MoveDiskParameters moveDiskParameters : moveDiskParamsList) {
            DiskImage diskImage = diskMap.get(moveDiskParameters.getImageId());
            if (diskImage.getPlugged() != plugged) {
                return false;
            }
        }

        return true;
    }

    private List<String> getDisksAliases(List<MoveDiskParameters> moveVmDisksParamsList) {
        List<String> disksAliases = new LinkedList<>();
        for (MoveDiskParameters moveDiskParameters : moveVmDisksParamsList) {
            DiskImage diskImage = diskMap.get(moveDiskParameters.getImageId());
            disksAliases.add(diskImage.getDiskAlias());
        }
        return disksAliases;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();

        for (MoveOrCopyImageGroupParameters parameters : getParameters().getParametersList()) {
            DiskImage diskImage = getDiskImageDao().get(parameters.getImageId());
            Guid diskId = diskImage == null ? Guid.Empty : diskImage.getId();
            permissionList.add(new PermissionSubject(diskId,
                    VdcObjectType.Disk,
                    ActionGroup.CONFIGURE_DISK_STORAGE));
        }

        return permissionList;
    }

    protected List<MoveDiskParameters> getMoveDiskParametersList() {
        return moveDiskParametersList;
    }

    protected List<LiveMigrateVmDisksParameters> getLiveMigrateVmDisksParametersList() {
        return liveMigrateVmDisksParametersList;
    }
}
