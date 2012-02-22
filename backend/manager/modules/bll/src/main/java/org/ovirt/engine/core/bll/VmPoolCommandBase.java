package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmType;
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
        List<vm_pool_map> vmPools = DbFacade.getInstance().getVmPoolDAO().getVmPoolsMapByVmPoolId(poolId);
        if (vmPools != null) {
            for (vm_pool_map map : vmPools) {
                if (CanAttacheVmToUser(map.getvm_guid())) {
                    return map.getvm_guid();
                }
            }
        }
        return Guid.Empty;
    }

    /**
     * Checks if a VM can be attached to a user.
     *
     * @param vm_guid
     *            the VM GUID to check.
     * @return True if can be attached, false otherwise.
     */
    protected static boolean CanAttacheVmToUser(Guid vm_guid) {
        // NOTE: We created the 'messages' variable since there are some methods
        // that don't check if 'messages' is null or not before adding items to
        // it (e.g. PerfromImagesCheck, CanFindVdsToRunOn).
        java.util.ArrayList<String> messages = new java.util.ArrayList<String>();
        boolean ret = IsVmFree(vm_guid, messages);
        if (ret) {
            VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDAO().get(vm_guid);
            if (vmDynamic.getstatus() != VMStatus.Down) {
                ret = false;
            }
        }
        return ret;
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
        if (DbFacade.getInstance().getDbUserDAO().getAllForVm(vmId).size() > 0) {
            returnValue = false;
            if (messages != null) {
                messages.add(VdcBllMessages.VM_POOL_CANNOT_ADD_VM_WITH_USERS_ATTACHED_TO_POOL.toString());
            }
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

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(getVmPoolId() == null ? null : getVmPoolId().getValue(), VdcObjectType.VmPool);
    }
}
