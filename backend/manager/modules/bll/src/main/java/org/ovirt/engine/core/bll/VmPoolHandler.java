package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.bll.validator.RunVmValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmPoolHandler implements BackendService {

    @FunctionalInterface
    public interface ErrorProcessor {

        void process(Guid vmId, List<String> errors);

    }

    private static final Logger log = LoggerFactory.getLogger(VmPoolHandler.class);

    @Inject
    private LockManager lockManager;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private IsoDomainListSynchronizer isoDomainListSynchronizer;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private SnapshotsValidator snapshotsValidator;

    public EngineLock createLock(Guid vmId) {
        return new EngineLock(
                RunVmCommandBase.getExclusiveLocksForRunVm(vmId, getLockMessage()),
                RunVmCommandBase.getSharedLocksForRunVm());
    }

    private String getLockMessage() {
        return EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED.name();
    }

    private boolean acquireLock(EngineLock lock) {
        boolean success = lockManager.acquireLock(lock).isAcquired();
        if (success) {
            log.info("Lock Acquired to object '{}'", lock);
        } else {
            log.info("Failed to Acquire Lock to object '{}'", lock);
        }
        return success;
    }

    public Guid acquireVm(Guid vmId, boolean leaveLocked) {
        if (!leaveLocked) {
            return vmId;
        }

        EngineLock lock = createLock(vmId);
        return acquireLock(lock) ? vmId : Guid.Empty;
    }

    private Stream<Guid> selectVms(Guid vmPoolId, VMStatus vmStatus, Predicate<Guid> vmIdFilter, boolean leaveLocked) {
        List<VmPoolMap> vmPoolMaps = vmPoolDao.getVmMapsInVmPoolByVmPoolIdAndStatus(vmPoolId, vmStatus);
        if (vmPoolMaps == null) {
            return Stream.empty();
        }

        return vmPoolMaps.stream()
                .map(VmPoolMap::getVmId)
                .filter(vmIdFilter)
                .map(vmId -> acquireVm(vmId, leaveLocked))
                .filter(vmId -> !Guid.Empty.equals(vmId));
    }

    public Stream<Guid> selectPrestartedVms(Guid vmPoolId, boolean isStatefulPool, ErrorProcessor errorProcessor) {
        return selectPrestartedVms(vmPoolId, isStatefulPool, errorProcessor, false);
    }

    public Stream<Guid> selectPrestartedVms(Guid vmPoolId, boolean isStatefulPool, ErrorProcessor errorProcessor, boolean leaveLocked) {
        return selectVms(vmPoolId,
                VMStatus.Up,
                vmId -> isPrestartedVmFree(vmId, isStatefulPool, errorProcessor),
                leaveLocked);
    }

    public Stream<Guid> selectNonPrestartedVms(Guid vmPoolId, ErrorProcessor errorProcessor) {
        return selectNonPrestartedVms(vmPoolId, errorProcessor, true);
    }

    public Stream<Guid> selectNonPrestartedVms(Guid vmPoolId, ErrorProcessor errorProcessor, boolean leaveLocked) {
        return selectVms(vmPoolId,
                VMStatus.Down,
                vmId -> isNonPrestartedVmFree(vmId, errorProcessor),
                leaveLocked);
    }

    public Guid selectPrestartedVm(Guid vmPoolId, boolean isStatefulPool, ErrorProcessor errorProcessor) {
        return selectPrestartedVms(vmPoolId, isStatefulPool, errorProcessor, false)
                .findFirst()
                .map(vmId -> acquireVm(vmId, false))
                .orElse(Guid.Empty);
    }

    public Guid selectNonPrestartedVm(Guid vmPoolId, ErrorProcessor errorProcessor) {
        return selectNonPrestartedVms(vmPoolId, errorProcessor, false)
                .findFirst()
                .map(vmId -> acquireVm(vmId, true))
                .orElse(Guid.Empty);
    }

    /**
     * Checks if a running VM is free.
     * @param vmId The VM GUID to check.
     * @param isStatefulPool True if the VM is part of a stateful pool, false otherwise.
     * @param errorProcessor the error messages processor object or lambda expression (may be <code>null</code>)
     * @return True if can be attached, false otherwise.
     */
    public boolean isPrestartedVmFree(Guid vmId, boolean isStatefulPool, ErrorProcessor errorProcessor) {
        // check that there is no user already attached to this VM
        // and make sure the VM is running statelessly
        List<String> messages = new ArrayList<>();
        boolean isFree = !vmAssignedToUser(vmId, messages)
                && (isStatefulPool && !vmIsStartedByRunOnce(vmId) || vmIsRunningStateless(vmId));
        if (errorProcessor != null && !messages.isEmpty()) {
            errorProcessor.process(vmId, messages);
        }
        return isFree;
    }

    /**
     * Check if a specific VM is free. A VM is considered free if it isn't attached to a user and not during preview
     * @param vmId The VM ID
     * @param errorProcessor the error messages processor object or lambda expression (may be <code>null</code>)
     * @return True if the VM is free, false otherwise
     */
    public boolean isNonPrestartedVmFree(Guid vmId, ErrorProcessor errorProcessor) {
        List<String> messages = new ArrayList<>();

        // check that there is no user already attached to this VM
        if (vmAssignedToUser(vmId, messages)) {
            return failVmFree(errorProcessor, vmId, messages);
        }

        // check that VN can be run
        if (!canRunPoolVm(vmId, messages)) {
            return failVmFree(errorProcessor, vmId, messages);
        }

        // check VM images
        ValidationResult vmDuringSnapshotResult = snapshotsValidator.vmNotDuringSnapshot(vmId);
        if (!vmDuringSnapshotResult.isValid()) {
            return failVmFree(errorProcessor, vmId, vmDuringSnapshotResult.getMessagesAsStrings());
        }

        ValidationResult vmInPreviewResult = snapshotsValidator.vmNotInPreview(vmId);
        if (!vmInPreviewResult.isValid()) {
            return failVmFree(errorProcessor, vmId, vmInPreviewResult.getMessagesAsStrings());
        }

        List<Disk> disks = diskDao.getAllForVm(vmId);
        List<DiskImage> vmImages = DisksFilter.filterImageDisks(disks, ONLY_NOT_SHAREABLE, ONLY_SNAPABLE);

        VM vm = vmDao.get(vmId);
        StoragePool sp = storagePoolDao.get(vm.getStoragePoolId());
        ValidationResult spUpResult = new StoragePoolValidator(sp).existsAndUp();
        if (!spUpResult.isValid()) {
            return failVmFree(errorProcessor, vmId, spUpResult.getMessagesAsStrings());
        }

        Guid storageDomainId = vmImages.size() > 0 ? vmImages.get(0).getStorageIds().get(0) : Guid.Empty;
        if (!Guid.Empty.equals(storageDomainId)) {
            StorageDomainValidator storageDomainValidator =
                    new StorageDomainValidator(storageDomainDao
                            .getForStoragePool(storageDomainId, sp.getId()));
            ValidationResult domainActiveResult = storageDomainValidator.isDomainExistAndActive();
            if (!domainActiveResult.isValid()) {
                return failVmFree(errorProcessor, vmId, domainActiveResult.getMessagesAsStrings());
            }
        }

        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(vmImages);
        ValidationResult disksNotLockedResult = diskImagesValidator.diskImagesNotLocked();
        if (!disksNotLockedResult.isValid()) {
            messages.addAll(disksNotLockedResult.getMessagesAsStrings());
            messages.addAll(disksNotLockedResult.getVariableReplacements());
            return failVmFree(errorProcessor, vmId, messages);
        }

        ValidationResult vmNotLockResult = new VmValidator(vm).vmNotLocked();
        if (!vmNotLockResult.isValid()) {
            return failVmFree(errorProcessor, vmId, vmNotLockResult.getMessagesAsStrings());
        }

        return true;
    }

    private boolean failVmFree(ErrorProcessor errorProcessor, Guid vmId, List<String> messages) {
        if (errorProcessor != null) {
            List<String> errors = new ArrayList<>(messages);
            errors.add(EngineMessage.VAR__TYPE__DESKTOP_POOL.toString());
            errors.add(EngineMessage.VAR__ACTION__ATTACH_DESKTOP_TO.toString());
            errorProcessor.process(vmId, errors);
        }
        return false;
    }

    private boolean vmIsStartedByRunOnce(Guid vmId) {
        VmDynamic vmDynamic = vmDynamicDao.get(vmId);
        return vmDynamic != null && vmDynamic.isRunOnce();
    }

    private boolean vmIsRunningStateless(Guid vmId) {
        return snapshotDao.exists(vmId, Snapshot.SnapshotType.STATELESS);
    }

    private boolean vmAssignedToUser(Guid vmId, List<String> messages) {
        if (dbUserDao.getAllForVm(vmId).size() > 0) {
            messages.add(EngineMessage.VM_POOL_CANNOT_ADD_VM_WITH_USERS_ATTACHED_TO_POOL.toString());
            return true;
        }
        return false;
    }

    private boolean canRunPoolVm(Guid vmId, List<String> messages) {
        VM vm = vmDao.get(vmId);
        if (vm == null) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND.name());
            return false;
        }

        // TODO: This is done to keep consistency with VmDao.getById.
        // It can probably be removed, but that requires some more research
        vmHandler.updateNetworkInterfacesFromDb(vm);

        RunVmParams runVmParams = new RunVmParams(vmId);

        return Injector.injectMembers(new RunVmValidator(vm, runVmParams, false, findActiveISODomain(vm.getStoragePoolId())))
                .canRunVm(
                        messages,
                        fetchStoragePool(vm.getStoragePoolId()),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        clusterDao.get(vm.getClusterId()),
                        false);
    }

    private Guid findActiveISODomain(Guid storagePoolId) {
        return isoDomainListSynchronizer.findActiveISODomain(storagePoolId);
    }

    private StoragePool fetchStoragePool(Guid storagePoolId) {
        return storagePoolDao.get(storagePoolId);
    }

}
