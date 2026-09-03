package org.ovirt.engine.core.bll;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies the NEXT_RUN configuration of a VM, if such is pending.
 *
 * <p>The configuration is applied when the VM goes down ({@link ProcessDownVmCommand}) and, as a fallback for the
 * cases in which the VM could not be updated at that point, right before the VM is started again
 * ({@link RunVmCommand}).
 */
@ApplicationScoped
public class NextRunConfigurationApplier {

    private static final Logger log = LoggerFactory.getLogger(NextRunConfigurationApplier.class);

    public enum Result {
        /** No NEXT_RUN configuration is pending for the VM */
        NOT_PENDING,
        APPLIED,
        /** The configuration was applied by re-creating the VM from a newer template version */
        TEMPLATE_VERSION_CHANGED,
        /** The VM is locked by another operation, the configuration is still pending */
        LOCKED
    }

    @Inject
    private BackendInternal backend;
    @Inject
    private LockManager lockManager;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private SnapshotsManager snapshotsManager;
    @Inject
    private VmNumaNodeDao vmNumaNodeDao;

    /**
     * Applies the pending NEXT_RUN configuration, if any, on the given VM.
     *
     * <p>Note that the given VM entity is updated in-place with the applied configuration.
     */
    public Result apply(VM vm) {
        return apply(vm, 0);
    }

    /**
     * Applies the pending NEXT_RUN configuration, if any, on the given VM.
     *
     * <p>Note that the given VM entity is updated in-place with the applied configuration.
     *
     * @param waitTimeoutMillis how long to wait for the UpdateVm lock to be released in
     * case it is currently held by another operation (e.g. a snapshot removal or a live
     * merge that started while the VM was still up); {@code 0} to only try once without
     * waiting
     */
    public Result apply(VM vm, long waitTimeoutMillis) {
        // Remove snapshot first, in case other update is in progress, it will block this one with exclusive lock
        // and any newer update should be preferred to this one.
        Snapshot runSnap = snapshotDao.get(vm.getId(), SnapshotType.NEXT_RUN);
        if (runSnap == null) {
            return Result.NOT_PENDING;
        }

        log.debug("Attempt to apply NEXT_RUN snapshot for VM '{}'", vm.getId());

        EngineLock updateVmLock = new EngineLock(
                UpdateVmCommand.getExclusiveLocksForUpdateVm(vm),
                UpdateVmCommand.getSharedLocksForUpdateVm(vm));
        if (!acquireUpdateVmLock(vm.getId(), updateVmLock, waitTimeoutMillis)) {
            log.warn("Could not acquire lock for UpdateVmCommand to apply Next Run Config of VM '{}'", vm.getId());
            return Result.LOCKED;
        }

        // Re-check under the lock: another thread (e.g. ProcessDownVmCommand or a
        // parallel RunVm attempt) might have applied the configuration while we were
        // waiting for the lock. Applying the stale snapshot would overwrite a possibly
        // newer configuration.
        Snapshot snapUnderLock = snapshotDao.get(vm.getId(), SnapshotType.NEXT_RUN);
        if (snapUnderLock == null || !snapUnderLock.getId().equals(runSnap.getId())) {
            lockManager.releaseLock(updateVmLock);
            return Result.NOT_PENDING;
        }

        snapshotDao.remove(snapUnderLock.getId());
        List<VmNumaNode> vmNumaNodeList = vmNumaNodeDao.getAllVmNumaNodeByVmId(vm.getId());
        Date originalCreationDate = vm.getVmCreationDate();
        snapshotsManager.updateVmFromConfiguration(vm, snapUnderLock.getVmConfiguration());
        // override creation date because the value in the config is the creation date of the config, not the vm
        vm.setVmCreationDate(originalCreationDate);
        boolean isNumaChanged = !vm.getvNumaNodeList().equals(vmNumaNodeList);

        ActionReturnValue result = backend.runInternalAction(
                ActionType.UpdateVm,
                createUpdateVmParameters(vm, isNumaChanged),
                ExecutionHandler.createInternalJobContext(updateVmLock));
        if (ActionType.UpdateVmVersion.equals(result.getActionReturnValue())) {
            return Result.TEMPLATE_VERSION_CHANGED;
        }
        return Result.APPLIED;
    }

    private boolean acquireUpdateVmLock(Guid vmId, EngineLock updateVmLock, long waitTimeoutMillis) {
        if (lockManager.acquireLock(updateVmLock).isAcquired()) {
            return true;
        }

        // The exclusive lock is on the VM name; waiting is only supported for a lock
        // holding at most one exclusive entry, as enforced by the lock manager.
        if (waitTimeoutMillis <= 0 || updateVmLock.getExclusiveLocks() == null
                || updateVmLock.getExclusiveLocks().size() > 1) {
            return false;
        }

        log.info("Failed to acquire lock for UpdateVmCommand to apply Next Run Config of VM '{}', " +
                "waiting up to '{}' ms for the lock to be released", vmId, waitTimeoutMillis);
        return lockManager.acquireLockWait(updateVmLock, waitTimeoutMillis).isAcquired();
    }

    private VmManagementParametersBase createUpdateVmParameters(VM vm, boolean isNumaChanged) {
        // clear non updateable fields got from config
        vm.setExportDate(null);
        vm.setOvfVersion(null);

        VmManagementParametersBase updateVmParams = new VmManagementParametersBase(vm);
        updateVmParams.setUpdateWatchdog(true);
        updateVmParams.setTpmEnabled(false);
        updateVmParams.setSoundDeviceEnabled(false);
        updateVmParams.setVirtioScsiEnabled(false);
        updateVmParams.setClearPayload(true);
        updateVmParams.setUpdateRngDevice(true);
        updateVmParams.setUpdateNuma(isNumaChanged);
        for (GraphicsType graphicsType : GraphicsType.values()) {
            updateVmParams.getGraphicsDevices().put(graphicsType, null);
        }

        for (VmDevice device : vm.getManagedVmDeviceMap().values()) {
            switch (device.getType()) {
                case WATCHDOG:
                    updateVmParams.setWatchdog(new VmWatchdog(device));
                    break;
                case SOUND:
                    updateVmParams.setSoundDeviceEnabled(true);
                    break;
                case CONTROLLER:
                    if (VmDeviceType.VIRTIOSCSI.getName().equals(device.getDevice())) {
                        updateVmParams.setVirtioScsiEnabled(true);
                    }
                    break;
                case DISK:
                    if (VmPayload.isPayload(device.getSpecParams())) {
                        updateVmParams.setVmPayload(new VmPayload(device));
                    }
                    break;
                case CONSOLE:
                    updateVmParams.setConsoleEnabled(true);
                    break;
                case RNG:
                    updateVmParams.setRngDevice(new VmRngDevice(device));
                    break;
                case GRAPHICS:
                    updateVmParams.getGraphicsDevices().put(GraphicsType.fromString(device.getDevice()),
                            new GraphicsDevice(device));
                    break;
                case TPM:
                    updateVmParams.setTpmEnabled(true);
                    break;
                case MDEV:
                    updateVmParams.getMdevs().put(device.getDeviceId(), device.getSpecParams());
                    break;
                default:
            }
        }

        // clear these fields as these are non updatable
        vm.getManagedVmDeviceMap().clear();
        vm.getUnmanagedDeviceList().clear();

        return updateVmParams;
    }
}
