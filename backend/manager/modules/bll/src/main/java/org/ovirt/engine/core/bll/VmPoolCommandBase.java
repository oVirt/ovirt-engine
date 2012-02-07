package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.stateless_vm_image_map;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("VmPoolName") })
public abstract class VmPoolCommandBase<T extends VmPoolParametersBase> extends CommandBase<T> {
    private vm_pools mVmPool;

    protected vm_pools getVmPool() {
        if (mVmPool == null && getVmPoolId() != null) {
            mVmPool = DbFacade.getInstance().getVmPoolDAO().get(getVmPoolId());
        }
        return mVmPool;
    }

    protected void setVmPool(vm_pools value) {
        mVmPool = value;
    }

    protected NGuid getVmPoolId() {
        return getParameters().getVmPoolId();
    }

    protected void setVmPoolId(NGuid value) {
        getParameters().setVmPoolId(value);
    }

    public String getVmPoolName() {
        return getVmPool() != null ? getVmPool().getvm_pool_name() : null;
    }

    @Override
    protected String getDescription() {
        return getVmPoolName();
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected VmPoolCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmPoolCommandBase(T parameters) {
        super(parameters);

    }

    public static Guid GetVmToAttach(NGuid poolId) {
        Guid vmGuid = Guid.Empty;
        vmGuid = getPrestartedVmToAttach(poolId);
        if (vmGuid == null || Guid.Empty.equals(vmGuid)) {
            vmGuid = getNonPrestartedVmToAttach(poolId);
        }
        return vmGuid;
    }

    protected static Guid getNonPrestartedVmToAttach(NGuid vmPoolId) {
        List<vm_pool_map> vmPoolMaps = DbFacade.getInstance().getVmPoolDAO()
        .getVmMapsInVmPoolByVmPoolIdAndStatus(vmPoolId, VMStatus.Down);
        if (vmPoolMaps != null) {
            for (vm_pool_map map : vmPoolMaps) {
                if (CanAttachNonPrestartedVmToUser(map.getvm_guid())) {
                    return map.getvm_guid();
                }
            }
        }
        return Guid.Empty;
    }

    protected static Guid getPrestartedVmToAttach(NGuid vmPoolId) {
        List<vm_pool_map> vmPoolMaps = DbFacade.getInstance().getVmPoolDAO()
        .getVmMapsInVmPoolByVmPoolIdAndStatus(vmPoolId, VMStatus.Up);
        if (vmPoolMaps != null) {
            for (vm_pool_map map : vmPoolMaps) {
                if (CanAttachPrestartedVmToUser(map.getvm_guid())) {
                    return map.getvm_guid();
                }
            }
        }
        return Guid.Empty;
    }

    protected static int getNumOfPrestartedVmsInPool(NGuid poolId) {
        List<vm_pool_map> vmPoolMaps = DbFacade.getInstance().getVmPoolDAO()
        .getVmMapsInVmPoolByVmPoolIdAndStatus(poolId, VMStatus.Up);
        int prestartedVmsInPool = 0;
        if (vmPoolMaps != null) {
            for (vm_pool_map map : vmPoolMaps) {
                if (CanAttachPrestartedVmToUser(map.getvm_guid())) {
                    prestartedVmsInPool++;
                }
            }
        }
        return prestartedVmsInPool;
    }

    protected static List<vm_pool_map> getListOfVmsInPool(NGuid poolId) {
        return DbFacade.getInstance().getVmPoolDAO().getVmPoolsMapByVmPoolId(poolId);
    }

    /**
     * Checks if a VM can be attached to a user.
     *
     * @param vm_guid
     *            the VM GUID to check.
     * @return True if can be attached, false otherwise.
     */
    protected static boolean CanAttachNonPrestartedVmToUser(Guid vm_guid) {
        return IsVmFree(vm_guid, new java.util.ArrayList<String>());
    }

    /**
     * Checks if a Prestarted Vm can be attached to a user.
     *
     * @param vmId
     *            the VM GUID to check.
     * @return True if can be attached, false otherwise.
     */
    protected static boolean CanAttachPrestartedVmToUser(Guid vmId) {
        boolean returnValue = true;
        java.util.ArrayList<String> messages = new java.util.ArrayList<String>();

        // check that there isn't another user already attached to this VM:
        if (vmAssignedToUser(vmId, messages)) {
            returnValue = false;
        }

        // Make sure the Vm is running stateless
        if (returnValue) {
            if (!vmIsRunningStateless(vmId, messages)) {
                returnValue = false;
            }
        }

        return returnValue;
    }

    private static boolean vmIsRunningStateless(Guid vmId, ArrayList<String> messages) {
        List<stateless_vm_image_map> list = DbFacade.getInstance().getDiskImageDAO()
        .getAllStatelessVmImageMapsForVm(vmId);
        if (list != null && list.size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if specific vm free. Vm considered free if it not attached to user
     * and not during trieng
     *
     * @param vmId
     *            The vm id.
     * @param messages
     *            The messages.
     * @return <c>true</c> if [is vm free] [the specified vm id]; otherwise,
     *         <c>false</c>.
     */
    protected static boolean IsVmFree(Guid vmId, java.util.ArrayList<String> messages) {
        boolean returnValue;

        // check that there isn't another user already attached to this VM:
        if (vmAssignedToUser(vmId, messages)) {
            returnValue = false;
        }

        // check that vm can be run:
        else if (!CanRunPoolVm(vmId, messages)) {
            returnValue = false;
        }

        // check vm images:
        else {
            ValidationResult vmDuringSnapshotResult =
                    new SnapshotsValidator().vmNotDuringSnapshot(vmId);
            if (!vmDuringSnapshotResult.isValid()) {
                messages.add(vmDuringSnapshotResult.getMessage().name());
                returnValue = false;
            } else {
                List<DiskImage> vmImages = DbFacade.getInstance().getDiskImageDAO().getAllForVm(vmId);
                Guid storageDomainId = vmImages.size() > 0 ? vmImages.get(0).getstorage_ids().get(0) : Guid.Empty;
                returnValue = ImagesHandler.PerformImagesChecks(vmId, messages,
                        DbFacade.getInstance().getVmDAO().getById(vmId).getstorage_pool_id(), storageDomainId, false, true,
                        false, false, true, false, !storageDomainId.equals(Guid.Empty));
            }

            if (!returnValue) {
                if (messages != null) {
                    messages.add(VdcBllMessages.VAR__TYPE__DESKTOP_POOL.toString());
                    messages.add(VdcBllMessages.VAR__ACTION__ATTACHE_DESKTOP_TO.toString());
                }
            }
        }

        return returnValue;
    }

    private static boolean vmAssignedToUser(Guid vmId, ArrayList<String> messages) {
        boolean vmAssignedToUser = false;
        if (DbFacade.getInstance().getDbUserDAO().getAllForVm(vmId).size() > 0) {
            vmAssignedToUser = true;
            if (messages != null) {
                messages.add(VdcBllMessages.VM_POOL_CANNOT_ADD_VM_WITH_USERS_ATTACHED_TO_POOL.toString());
            }
        }
        return vmAssignedToUser;
    }

    protected static boolean CanRunPoolVm(Guid vmId, java.util.ArrayList<String> messages) {
        VM vm = DbFacade.getInstance().getVmDAO().getById(vmId);
        RunVmParams tempVar = new RunVmParams(vmId);
        tempVar.setUseVnc(vm.getvm_os().isLinux() || vm.getvm_type() == VmType.Server);
        RunVmParams runVmParams = tempVar;
        VdsSelector vdsSelector = new VdsSelector(vm,
                ((runVmParams.getDestinationVdsId()) != null) ? runVmParams.getDestinationVdsId()
                        : vm.getdedicated_vm_for_vds(), true);
        return RunVmCommand.CanRunVm(vm, messages, runVmParams, vdsSelector, new SnapshotsValidator());
    }

    @Override
    protected List<tags> GetTagsAttachedToObject() {
        return DbFacade.getInstance().getTagDAO()
                .getAllForVmPools((getParameters().getVmPoolId()).toString());
    }

    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getVmPoolId() == null ? null : getVmPoolId().getValue(),
                VdcObjectType.VmPool,
                getActionType().getActionGroup()));
        return permissionList;
    }
}
