package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
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
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.utils.MultiValueMapUtils;

public class MoveDisksCommand<T extends MoveDisksParameters> extends CommandBase<T> {

    private static final long serialVersionUID = -4650248318102141136L;

    private List<VdcReturnValueBase> vdcReturnValues = new ArrayList<VdcReturnValueBase>();
    private List<MoveDiskParameters> moveParametersList = new ArrayList<MoveDiskParameters>();
    private Map<Guid, List<LiveMigrateDiskParameters>> vmsLiveMigrateParametersMap =
            new HashMap<Guid, List<LiveMigrateDiskParameters>>();

    public MoveDisksCommand(Guid commandId) {
        super(commandId);
    }

    public MoveDisksCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        if (!moveParametersList.isEmpty()) {
            vdcReturnValues.addAll(Backend.getInstance().RunMultipleActions(VdcActionType.MoveOrCopyDisk,
                    getMoveDisksParametersList(), false));
        }

        if (!vmsLiveMigrateParametersMap.isEmpty()) {
            vdcReturnValues.addAll(Backend.getInstance().RunMultipleActions(VdcActionType.LiveMigrateVmDisks,
                    getLiveMigrateDisksParametersList(), false));
        }

        handleChildReturnValue();
        setSucceeded(true);
    }

    private void handleChildReturnValue() {
        for (VdcReturnValueBase vdcReturnValue : vdcReturnValues) {
            getReturnValue().getCanDoActionMessages().addAll(vdcReturnValue.getCanDoActionMessages());
            getReturnValue().setCanDoAction(getReturnValue().getCanDoAction() && vdcReturnValue.getCanDoAction());
        }
    }

    @Override
    protected boolean canDoAction() {
        if (getParameters().getParametersList().isEmpty()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED);
        }

        return updateParameters();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    /**
     * For each specified MoveDiskParameters, decide whether it should be moved
     * using 'regular' move or using live migrate command.
     *
     * @return false if the command should fail on canDoAction; otherwise, true
     */
    private boolean updateParameters() {
        for (MoveDiskParameters moveDiskParameters : getParameters().getParametersList()) {
            DiskImage diskImage = getDiskImageDao().get(moveDiskParameters.getImageId());
            if (diskImage == null) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
            }

            List<VM> allVms = getVmDAO().getVmsListForDisk(diskImage.getId());
            VM vm = !allVms.isEmpty() ? allVms.get(0) : null;

            if (vm != null && !validate(createSnapshotsValidator().vmNotInPreview(vm.getId()))) {
                return false;
            }

            if (vm == null || isVmDown(vm)) {
                moveParametersList.add(moveDiskParameters);
            }
            else if (isVmRunning(vm)) {
                MultiValueMapUtils.addToMap(vm.getId(),
                        createLiveMigrateDiskParameters(moveDiskParameters, vm.getId()),
                        vmsLiveMigrateParametersMap);
            }
            else {
                addCanDoActionMessage(String.format("$%1$s %2$s", "VmName", vm.getName()));
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP);
            }
        }

        return true;
    }

    private boolean isVmRunning(VM vm) {
        return vm.getStatus().isUpOrPaused() && vm.getRunOnVds() != null && !vm.getRunOnVds().equals(Guid.Empty);
    }

    private boolean isVmDown(VM vm) {
        return vm.getStatus() == VMStatus.Down;
    }

    private LiveMigrateDiskParameters createLiveMigrateDiskParameters(MoveDiskParameters moveDiskParameters, Guid vmId) {
        return new LiveMigrateDiskParameters(moveDiskParameters.getImageId(),
                moveDiskParameters.getSourceDomainId(),
                moveDiskParameters.getStorageDomainId(),
                vmId,
                moveDiskParameters.getQuotaId());
    }

    protected ArrayList<VdcActionParametersBase> getMoveDisksParametersList() {
        for (MoveDiskParameters moveDiskParameters : moveParametersList) {
            moveDiskParameters.setSessionId(getParameters().getSessionId());
        }

        return new ArrayList<VdcActionParametersBase>(moveParametersList);
    }

    protected ArrayList<VdcActionParametersBase> getLiveMigrateDisksParametersList() {
        ArrayList<LiveMigrateVmDisksParameters> liveMigrateDisksParametersList =
                new ArrayList<LiveMigrateVmDisksParameters>();

        for (Map.Entry<Guid, List<LiveMigrateDiskParameters>> entry : vmsLiveMigrateParametersMap.entrySet()) {
            LiveMigrateVmDisksParameters liveMigrateDisksParameters =
                    new LiveMigrateVmDisksParameters(entry.getValue(), entry.getKey());

            liveMigrateDisksParameters.setParentCommand(VdcActionType.MoveDisks);
            liveMigrateDisksParameters.setSessionId(getParameters().getSessionId());

            liveMigrateDisksParametersList.add(liveMigrateDisksParameters);
        }

        return new ArrayList<VdcActionParametersBase>(liveMigrateDisksParametersList);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();

        for (MoveOrCopyImageGroupParameters parameters : getParameters().getParametersList()) {
            DiskImage diskImage = getDiskImageDao().get(parameters.getImageId());
            if (diskImage != null) {
                permissionList.add(new PermissionSubject(diskImage.getId(),
                        VdcObjectType.Disk,
                        ActionGroup.CONFIGURE_DISK_STORAGE));
            }
        }

        return permissionList;
    }

    protected DiskImageDAO getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
    }

    protected SnapshotsValidator createSnapshotsValidator() {
        return new SnapshotsValidator();
    }
}
