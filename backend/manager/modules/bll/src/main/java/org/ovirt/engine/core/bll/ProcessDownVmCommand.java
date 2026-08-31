package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DetachUserFromVmFromPoolParameters;
import org.ovirt.engine.core.common.action.ProcessDownVmParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class ProcessDownVmCommand<T extends ProcessDownVmParameters> extends CommandBase<T> {

    private static final Logger log = LoggerFactory.getLogger(ProcessDownVmCommand.class);

    /**
     * How long to wait for the UpdateVm lock when applying the next run configuration of a
     * VM that went down, in case the lock is held by a still-running asynchronous command
     * (e.g. snapshot removal or live merge) that started while the VM was still up.
     */
    private static final long NEXT_RUN_LOCK_TIMEOUT_MILLIS = 10 * 60 * 1000L;

    private boolean templateVersionChanged;

    @Inject
    private HostDeviceManager hostDeviceManager;
    @Inject
    private HostLocking hostLocking;
    @Inject
    private NetworkDeviceHelper networkDeviceHelper;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private NextRunConfigurationApplier nextRunConfigurationApplier;
    private VmPool vmPoolCached;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private VmNicDao vmNicDao;

    protected ProcessDownVmCommand(Guid commandId) {
        super(commandId);
    }

    public ProcessDownVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVmId(getParameters().getId());
    }

    private VmPool getVmPoolCached() {
        if (vmPoolCached == null && getVm().getVmPoolId() != null) {
            vmPoolCached = vmPoolDao.get(getVm().getVmPoolId());
        }
        return vmPoolCached;
    }

    private VmPoolType getVmPoolType() {
        VmPool pool = getVmPoolCached();
        return (pool != null) ? pool.getVmPoolType() : null;
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        return true;
    }

    private boolean isRemovingVmPool() {
        VmPool vmPool = getVmPoolCached();
        return vmPool != null ? vmPool.isBeingDestroyed() : false;
    }

    @Override
    protected void executeCommand() {
        boolean removingVmPool = isRemovingVmPool();

        if (!removingVmPool) {
            applyNextRunConfiguration();

            boolean removedStatelessSnapshot = detachUsers();
            if (!removedStatelessSnapshot && !templateVersionChanged) {
                // If template version didn't change, and we are dealing with a prestarted Vm
                // or a regular Vm - clean stateless images
                // Otherwise this was already done in DetachUserFromVmFromPoolCommand \ updateVmVersionCommand->RemoveVmCommand
                removeVmStatelessImages();
            }
        }

        getQuotaManager().rollbackQuotaByVmId(getVmId());

        if (!removingVmPool) {
            removeStatelessVmUnmanagedDevices();
            VmManager vmManager = resourceManager.getVmManager(getVmId(), false);
            if (vmManager != null) {
                vmManager.rebootCleanup();
            }

            boolean vmHasDirectPassthroughDevices = releaseUsedHostDevices();

            Guid hostId = cleanupVfs();
            // Only single dedicated host allowed for host devices, verified on validates
            Guid alternativeHostsList = vmHasDirectPassthroughDevices ? getVm().getDedicatedVmForVdsList().get(0) : null;
            refreshHostIfNeeded(hostId == null ? alternativeHostsList : hostId);
        }

        if (getParameters().getHostId() != null) {
            VdsManager vdsManager = resourceManager.getVdsManager(getParameters().getHostId(), false);
            if (vdsManager != null) {
                vdsManager.unpinVmCpus(getVm().getId());
            }
        }

        managedBlockStorageCommandUtil.disconnectManagedBlockStorageDisks(getVm(), vmHandler);
        vmNicDao.setVmInterfacesSyncedForVm(getVmId());
    }

    private boolean releaseUsedHostDevices() {
        if (hostDeviceManager.checkVmNeedsDirectPassthrough(getVm())) {
            try {
                // Only single dedicated host allowed for host devices, verified on validates
                hostLocking.acquireHostDevicesLock(getVm().getDedicatedVmForVdsList().get(0));
                hostDeviceManager.freeVmHostDevices(getVmId());
            } finally {
                // Only single dedicated host allowed for host devices, verified on validates
                hostLocking.releaseHostDevicesLock(getVm().getDedicatedVmForVdsList().get(0));
            }
            return true;
        }

        return false;
    }

    private Guid cleanupVfs() {
        return networkDeviceHelper.removeVmIdFromVfs(getVmId());
    }

    private void refreshHostIfNeeded(Guid hostId) {
        // refresh host to get the host devices that were detached from the VM and re-attached to the host
        if (!getParameters().isSkipHostRefresh() && hostId != null) {
            runInternalAction(ActionType.RefreshHost, new VdsActionParameters(hostId));
        }
    }

    private boolean detachUsers() {
        // check if this is a VM from a VM pool
        if (getVm().getVmPoolId() == null) {
            return false;
        }

        List<DbUser> users = dbUserDao.getAllForVm(getVmId());
        // check if this VM is attached to a user
        if (users == null || users.isEmpty()) {
            // if not, check if new version or need to restore stateless
            if (!templateVersionChanged) { // if template version was changed, no need to restore
                runInternalActionWithTasksContext(ActionType.RestoreStatelessVm,
                        new VmOperationParameterBase(getVmId()),
                        getLock());
            }
            return true;
        }
        if (getVmPoolType() == VmPoolType.AUTOMATIC) {
            // should be only one user in the collection
            for (DbUser dbUser : users) {
                runInternalActionWithTasksContext(ActionType.DetachUserFromVmFromPool,
                        new DetachUserFromVmFromPoolParameters(getVm().getVmPoolId(),
                                dbUser.getId(),
                                getVmId(),
                                !templateVersionChanged), getLock());
            }

            return true;
        }

        return false;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    /**
     * Remove VM's unmanaged devices that are created during run-once or stateless run.
     */
    private void removeStatelessVmUnmanagedDevices() {
        if (getVm().isStateless() || getVm().isRunOnce()) {
            List<VmDevice> unmanagedVmDevices = vmDeviceDao.getUnmanagedDevicesByVmId(getVmId());
            vmDeviceDao.removeAllInBatch(unmanagedVmDevices.stream()
                    // do not remove device if appears in white list
                    .filter(device -> !VmDeviceCommonUtils.isInWhiteList(device.getType(), device.getDevice()))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Update VM configuration with NEXT_RUN configuration, if exists.
     *
     * <p>If the configuration cannot be applied now, e.g. because the VM is locked by another operation, it stays
     * pending and {@link RunVmCommand} applies it before the VM is started again.
     */
    private void applyNextRunConfiguration() {
        if (getVm().getStatus() == VMStatus.Suspended) {
            return;
        }

        // The VM is already down, hence the only reason for the UpdateVm lock to be
        // unavailable is an asynchronous command that started while the VM was still
        // running (e.g. snapshot removal or live merge) and is still holding the VM
        // lock. Waiting for it is safe; if it still fails after the timeout, the
        // configuration stays pending and RunVmCommand applies it before the VM is
        // started again.
        if (nextRunConfigurationApplier.apply(getVm(), NEXT_RUN_LOCK_TIMEOUT_MILLIS)
                == NextRunConfigurationApplier.Result.TEMPLATE_VERSION_CHANGED) {
            templateVersionChanged = true;
        }
    }

    private void removeVmStatelessImages() {
        if (snapshotDao.exists(getVmId(), SnapshotType.STATELESS) && getVmPoolType() != VmPoolType.MANUAL) {
            log.info("Deleting snapshot for stateless vm '{}'", getVmId());
            runInternalAction(ActionType.RestoreStatelessVm,
                    new VmOperationParameterBase(getVmId()),
                    ExecutionHandler.createDefaultContextForTasks(getContext(), getLock()));
        }
    }

}
