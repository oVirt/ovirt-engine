package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSyncronizer;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.RunVmValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmPoolDao;

public abstract class VmPoolCommandBase<T extends VmPoolParametersBase> extends CommandBase<T> {
    private VmPool vmPool;

    protected VmPool getVmPool() {
        if (vmPool == null && getVmPoolId() != null) {
            vmPool = getVmPoolDao().get(getVmPoolId());
        }
        return vmPool;
    }

    protected void setVmPool(VmPool value) {
        vmPool = value;
    }

    protected Guid getVmPoolId() {
        return getParameters().getVmPoolId();
    }

    protected void setVmPoolId(Guid value) {
        getParameters().setVmPoolId(value);
    }

    public String getVmPoolName() {
        return getVmPool() != null ? getVmPool().getName() : null;
    }

    @Override
    protected String getDescription() {
        return getVmPoolName();
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected VmPoolCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmPoolCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected static int getNumOfPrestartedVmsInPool(VmPool pool, ArrayList<String> messages) {
        List<VM> vmsInPool = DbFacade.getInstance().getVmDao().getAllForVmPool(pool.getVmPoolId());
        int numOfPrestartedVmsInPool = 0;
        if (vmsInPool != null) {
            for (VM vm : vmsInPool) {
                if (vm.isStartingOrUp() && canAttachPrestartedVmToUser(vm.getId(), pool.isStateful(), messages)) {
                    ++numOfPrestartedVmsInPool;
                }
            }
        }
        return numOfPrestartedVmsInPool;
    }

    protected static List<VmPoolMap> getListOfVmsInPool(Guid poolId) {
        return DbFacade.getInstance().getVmPoolDao().getVmPoolsMapByVmPoolId(poolId);
    }

    /**
     * Checks if a VM can be attached to a user.
     * @param vmId
     *            the VM GUID to check.
     * @return True if can be attached, false otherwise.
     */
    protected static boolean canAttachNonPrestartedVmToUser(Guid vmId, List<String> messages) {
        return isVmFree(vmId, messages);
    }

    /**
     * Checks if a running Vm can be attached to a user.
     * @param vmId
     *            the VM GUID to check.
     * @param isStatefulPool True if the VM is part of a stateful pool, false otherwise.
     * @return True if can be attached, false otherwise.
     */
    protected static boolean canAttachPrestartedVmToUser(Guid vmId, boolean isStatefulPool, List<String> messages) {
        // check that there isn't another user already attached to this VM
        // and make sure the Vm is running stateless
        return !vmAssignedToUser(vmId, messages)
                && (isStatefulPool && !vmIsStartedByRunOnce(vmId) || vmIsRunningStateless(vmId));
    }

    private static boolean vmIsStartedByRunOnce(Guid vmId) {
        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(vmId);
        return vmDynamic != null && vmDynamic.isRunOnce();
    }

    private static boolean vmIsRunningStateless(Guid vmId) {
        return DbFacade.getInstance().getSnapshotDao().exists(vmId, SnapshotType.STATELESS);
    }

    /**
     * Check if a specific VM is free. A VM is considered free if it isn't attached to a user and not during preview
     * @param vmId
     *            The vm id.
     * @param messages
     *            The messages.
     * @return <code>true</code> if [is vm free] [the specified vm id]; otherwise, <code>false</code>.
     */
    protected static boolean isVmFree(Guid vmId, List<String> messages) {
        // check that there isn't another user already attached to this VM:
        if (vmAssignedToUser(vmId, messages)) {
            return failVmFree(messages);
        }

        // check that vm can be run:
        if (!canRunPoolVm(vmId, messages)) {
            return failVmFree(messages);
        }

        // check vm images:
        SnapshotsValidator snapshotsValidator = new SnapshotsValidator();
        ValidationResult vmDuringSnapshotResult = snapshotsValidator.vmNotDuringSnapshot(vmId);
        if (!vmDuringSnapshotResult.isValid()) {
            return failVmFree(messages, vmDuringSnapshotResult.getMessage().name());
        }

        ValidationResult vmInPreviewResult = snapshotsValidator.vmNotInPreview(vmId);
        if (!vmInPreviewResult.isValid()) {
            return failVmFree(messages, vmInPreviewResult.getMessage().name());
        }

        List<Disk> disks = DbFacade.getInstance().getDiskDao().getAllForVm(vmId);
        List<DiskImage> vmImages = ImagesHandler.filterImageDisks(disks, true, true, false);

        VM vm = DbFacade.getInstance().getVmDao().get(vmId);
        StoragePool sp = DbFacade.getInstance().getStoragePoolDao().get(vm.getStoragePoolId());
        ValidationResult spUpResult = new StoragePoolValidator(sp).isUp();
        if (!spUpResult.isValid()) {
            return failVmFree(messages, spUpResult.getMessage().name());
        }

        Guid storageDomainId = vmImages.size() > 0 ? vmImages.get(0).getStorageIds().get(0) : Guid.Empty;
        if (!Guid.Empty.equals(storageDomainId)) {
            StorageDomainValidator storageDomainValidator =
                    new StorageDomainValidator(DbFacade.getInstance()
                            .getStorageDomainDao()
                            .getForStoragePool(storageDomainId, sp.getId()));
            ValidationResult domainActiveResult = storageDomainValidator.isDomainExistAndActive();
            if (!domainActiveResult.isValid()) {
                return failVmFree(messages, domainActiveResult.getMessage().name());
            }
        }

        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(vmImages);
        ValidationResult disksNotLockedResult = diskImagesValidator.diskImagesNotLocked();
        if (!disksNotLockedResult.isValid()) {
            List<String> messagesToAdd = new LinkedList<>();
            messagesToAdd.add(disksNotLockedResult.getMessage().name());
            messagesToAdd.addAll(disksNotLockedResult.getVariableReplacements());
            return failVmFree(messages, messagesToAdd);
        }

        ValidationResult vmNotLockResult = new VmValidator(vm).vmNotLocked();
        if (!vmNotLockResult.isValid()) {
            return failVmFree(messages, vmNotLockResult.getMessage().name());
        }

        return true;
    }

    private static boolean failVmFree(List<String> messages, String... messagesToAdd) {
        for (String messageToAdd : messagesToAdd) {
            messages.add(messageToAdd);
        }
        return failVmFree(messages, Arrays.asList(messagesToAdd));
    }

    private static boolean failVmFree(List<String> messages, List<String> messagesToAdd) {
        messages.addAll(messagesToAdd);
        messages.add(EngineMessage.VAR__TYPE__DESKTOP_POOL.toString());
        messages.add(EngineMessage.VAR__ACTION__ATTACH_DESKTOP_TO.toString());
        return false;
    }

    private static boolean vmAssignedToUser(Guid vmId, List<String> messages) {
        if (DbFacade.getInstance().getDbUserDao().getAllForVm(vmId).size() > 0) {
            messages.add(EngineMessage.VM_POOL_CANNOT_ADD_VM_WITH_USERS_ATTACHED_TO_POOL.toString());
            return true;
        }
        return false;
    }

    protected static boolean canRunPoolVm(Guid vmId, List<String> messages) {
        VM vm = DbFacade.getInstance().getVmDao().get(vmId);
        if (vm == null) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND.name());
            return false;
        }

        // TODO: This is done to keep consistency with VmDao.getById.
        // It can probably be removed, but that requires some more research
        VmHandler.updateNetworkInterfacesFromDb(vm);

        RunVmParams runVmParams = new RunVmParams(vmId);

        return new RunVmValidator(vm, runVmParams, false, findActiveISODomain(vm.getStoragePoolId()))
                .canRunVm(
                        messages,
                        fetchStoragePool(vm.getStoragePoolId()),
                        Collections.<Guid> emptyList(),
                        null,
                        null,
                        DbFacade.getInstance().getClusterDao().get(vm.getClusterId()));
    }

    private static Guid findActiveISODomain(Guid storagePoolId) {
        return IsoDomainListSyncronizer.getInstance().findActiveISODomain(storagePoolId);
    }

    private static StoragePool fetchStoragePool(Guid storagePoolId) {
        return DbFacade.getInstance().getStoragePoolDao().get(storagePoolId);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getVmPoolId(),
                VdcObjectType.VmPool,
                getActionType().getActionGroup()));
        return permissionList;
    }

    protected VmPoolDao getVmPoolDao() {
        return getDbFacade().getVmPoolDao();
    }
}
