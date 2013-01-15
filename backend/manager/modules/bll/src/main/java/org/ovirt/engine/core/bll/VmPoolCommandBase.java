package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.dao.VmPoolDAO;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;

@CustomLogFields({ @CustomLogField("VmPoolName") })
public abstract class VmPoolCommandBase<T extends VmPoolParametersBase> extends CommandBase<T> {
    private vm_pools mVmPool;

    protected vm_pools getVmPool() {
        if (mVmPool == null && getVmPoolId() != null) {
            mVmPool = getVmPoolDAO().get(getVmPoolId());
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
     * @param commandId
     */
    protected VmPoolCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmPoolCommandBase(T parameters) {
        super(parameters);

    }

    public static Guid getVmToAttach(NGuid poolId) {
        Guid vmGuid = Guid.Empty;
        vmGuid = getPrestartedVmToAttach(poolId);
        if (vmGuid == null || Guid.Empty.equals(vmGuid)) {
            vmGuid = getNonPrestartedVmToAttach(poolId);
        }
        return vmGuid;
    }

    protected static Guid getNonPrestartedVmToAttach(NGuid vmPoolId) {
        List<VmPoolMap> vmPoolMaps = DbFacade.getInstance().getVmPoolDao()
                .getVmMapsInVmPoolByVmPoolIdAndStatus(vmPoolId, VMStatus.Down);
        if (vmPoolMaps != null) {
            for (VmPoolMap map : vmPoolMaps) {
                if (canAttachNonPrestartedVmToUser(map.getvm_guid())) {
                    return map.getvm_guid();
                }
            }
        }
        return Guid.Empty;
    }

    protected static Guid getPrestartedVmToAttach(NGuid vmPoolId) {
        List<VmPoolMap> vmPoolMaps = DbFacade.getInstance().getVmPoolDao()
                .getVmMapsInVmPoolByVmPoolIdAndStatus(vmPoolId, VMStatus.Up);
        if (vmPoolMaps != null) {
            for (VmPoolMap map : vmPoolMaps) {
                if (canAttachPrestartedVmToUser(map.getvm_guid())) {
                    return map.getvm_guid();
                }
            }
        }
        return Guid.Empty;
    }

    protected static int getNumOfPrestartedVmsInPool(NGuid poolId) {
        List<VmPoolMap> vmPoolMaps = DbFacade.getInstance().getVmPoolDao()
                .getVmMapsInVmPoolByVmPoolIdAndStatus(poolId, VMStatus.Up);
        int prestartedVmsInPool = 0;
        if (vmPoolMaps != null) {
            for (VmPoolMap map : vmPoolMaps) {
                if (canAttachPrestartedVmToUser(map.getvm_guid())) {
                    prestartedVmsInPool++;
                }
            }
        }
        return prestartedVmsInPool;
    }

    protected static List<VmPoolMap> getListOfVmsInPool(NGuid poolId) {
        return DbFacade.getInstance().getVmPoolDao().getVmPoolsMapByVmPoolId(poolId);
    }

    /**
     * Checks if a VM can be attached to a user.
     * @param vm_guid
     *            the VM GUID to check.
     * @return True if can be attached, false otherwise.
     */
    protected static boolean canAttachNonPrestartedVmToUser(Guid vm_guid) {
        return isVmFree(vm_guid, new java.util.ArrayList<String>());
    }

    /**
     * Checks if a running Vm can be attached to a user.
     * @param vmId
     *            the VM GUID to check.
     * @return True if can be attached, false otherwise.
     */
    protected static boolean canAttachPrestartedVmToUser(Guid vmId) {
        boolean returnValue = true;
        java.util.ArrayList<String> messages = new java.util.ArrayList<String>();

        // check that there isn't another user already attached to this VM:
        if (vmAssignedToUser(vmId, messages)) {
            returnValue = false;
        }

        // Make sure the Vm is running stateless
        if (returnValue) {
            if (!vmIsRunningStateless(vmId)) {
                returnValue = false;
            }
        }

        return returnValue;
    }

    private static boolean vmIsRunningStateless(Guid vmId) {
        return DbFacade.getInstance().getSnapshotDao().exists(vmId, SnapshotType.STATELESS);
    }

    /**
     * Check if specific vm free. Vm considered free if it not attached to user and not during trieng
     * @param vmId
     *            The vm id.
     * @param messages
     *            The messages.
     * @return <c>true</c> if [is vm free] [the specified vm id]; otherwise, <c>false</c>.
     */
    protected static boolean isVmFree(Guid vmId, java.util.ArrayList<String> messages) {
        boolean returnValue;

        // check that there isn't another user already attached to this VM:
        if (vmAssignedToUser(vmId, messages)) {
            returnValue = false;
        }

        // check that vm can be run:
        else if (!canRunPoolVm(vmId, messages)) {
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
                List<Disk> disks = DbFacade.getInstance().getDiskDao().getAllForVm(vmId);
                List<DiskImage> vmImages = ImagesHandler.filterImageDisks(disks, true, true);
                Guid storageDomainId = vmImages.size() > 0 ? vmImages.get(0).getstorage_ids().get(0) : Guid.Empty;
                VM vm = DbFacade.getInstance().getVmDao().get(vmId);
                returnValue =
                        ImagesHandler.PerformImagesChecks(vm,
                                messages,
                                vm.getStoragePoolId(),
                                storageDomainId,
                                false,
                                true,
                                false,
                                false,
                                true,
                                !Guid.Empty.equals(storageDomainId),
                                true,
                                disks);
            }

            if (!returnValue) {
                if (messages != null) {
                    messages.add(VdcBllMessages.VAR__TYPE__DESKTOP_POOL.toString());
                    messages.add(VdcBllMessages.VAR__ACTION__ATTACH_DESKTOP_TO.toString());
                }
            }
        }

        return returnValue;
    }

    private static boolean vmAssignedToUser(Guid vmId, ArrayList<String> messages) {
        boolean vmAssignedToUser = false;
        if (DbFacade.getInstance().getDbUserDao().getAllForVm(vmId).size() > 0) {
            vmAssignedToUser = true;
            if (messages != null) {
                messages.add(VdcBllMessages.VM_POOL_CANNOT_ADD_VM_WITH_USERS_ATTACHED_TO_POOL.toString());
            }
        }
        return vmAssignedToUser;
    }

    protected static boolean canRunPoolVm(Guid vmId, java.util.ArrayList<String> messages) {
        VM vm = DbFacade.getInstance().getVmDao().get(vmId);

        // TODO: This is done to keep consistency with VmDAO.getById.
        // It can probably be removed, but that requires some more research
        VmHandler.updateNetworkInterfacesFromDb(vm);

        RunVmParams tempVar = new RunVmParams(vmId);
        tempVar.setUseVnc(vm.getVmOs().isLinux() || vm.getVmType() == VmType.Server);
        RunVmParams runVmParams = tempVar;
        VdsSelector vdsSelector = new VdsSelector(vm,
                ((runVmParams.getDestinationVdsId()) != null) ? runVmParams.getDestinationVdsId()
                        : vm.getDedicatedVmForVds(), true, new VdsFreeMemoryChecker(new NonWaitingDelayer()));
        return VmRunHandler.getInstance().canRunVm(vm,
                messages,
                runVmParams,
                vdsSelector,
                new SnapshotsValidator(),
                getVmPropertiesUtils());
    }

    protected static VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    @Override
    protected List<tags> getTagsAttachedToObject() {
        return DbFacade.getInstance().getTagDao()
                .getAllForVmPools((getParameters().getVmPoolId()).toString());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getVmPoolId() == null ? null : getVmPoolId().getValue(),
                VdcObjectType.VmPool,
                getActionType().getActionGroup()));
        return permissionList;
    }

    public static boolean isPrestartedVmForAssignment(Guid vm_guid) {
        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(vm_guid);
        if (vmDynamic != null && vmDynamic.getstatus() == VMStatus.Up && canAttachPrestartedVmToUser(vm_guid)) {
            return true;
        } else {
            return false;
        }
    }

    protected VmPoolDAO getVmPoolDAO() {
        return getDbFacade().getVmPoolDao();
    }
}
