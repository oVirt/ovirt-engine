package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HibernateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class HibernateVmCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {
    private static final Logger log = LoggerFactory.getLogger(HibernateVmCommand.class);

    private boolean isHibernateVdsProblematic;
    private Guid cachedStorageDomainId;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected HibernateVmCommand(Guid commandId) {
        super(commandId);
    }

    public HibernateVmCommand(T parameters) {
        super(parameters);
        if (getVm() != null) {
            setStoragePoolId(getVm().getStoragePoolId());
            parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    /**
     * Finds an active data/master storage domain which has enough space to store the hibernation volumes
     *
     * @return storage domain id or null if no suitable storage domain exists
     */
    @Override
    public Guid getStorageDomainId() {
        if (cachedStorageDomainId == null) {
            List<DiskImage> diskDummiesForMemSize = MemoryUtils.createDiskDummies(
                    getVm().getTotalMemorySizeInBytes(),
                    MemoryUtils.META_DATA_SIZE_IN_BYTES);
            StorageDomain storageDomain = MemoryStorageHandler.getInstance().findStorageDomainForMemory(
                    getStoragePoolId(), diskDummiesForMemSize,
                    ImagesHandler.filterImageDisks(getDiskDao().getAllForVm(getVmId()), false, false, false), getVm());
            if (storageDomain != null) {
                cachedStorageDomainId = storageDomain.getId();
            }
        }
        return cachedStorageDomainId;
    }

    @Override
    protected void perform() {
        DiskImage memoryDisk = MemoryUtils.createMemoryDiskForVm(getVm(), getStorageDomain().getStorageType());
        Guid dumpDiskId = addDisk(memoryDisk);

        DiskImage metaDataDisk = MemoryUtils.createMetadataDiskForVm(getVm());
        Guid metadataDiskId = addDisk(metaDataDisk);

        DiskImage dumpDisk = getDisk(dumpDiskId);
        DiskImage metadataDisk = getDisk(metadataDiskId);

        getSnapshotDao().updateHibernationMemory(getVmId(),
                dumpDisk.getId(), metadataDisk.getId(),
                createHibernationVolumeString(dumpDisk, metadataDisk));

        setSucceeded(true);
    }

    private String createHibernationVolumeString(DiskImage dumpDisk, DiskImage metadataDisk) {
        return MemoryUtils.createMemoryStateString(
                getStorageDomainId(), getStoragePoolId(),
                dumpDisk.getId(), dumpDisk.getImageId(),
                metadataDisk.getId(), metadataDisk.getImageId());
    }

    private DiskImage getDisk(Guid diskId) {
        return (DiskImage) getDiskDao().get(diskId);
    }

    private Guid addDisk(DiskImage disk) {
        VdcReturnValueBase returnValue = runInternalActionWithTasksContext(
                VdcActionType.AddDisk,
                buildAddDiskParameters(disk));

        if (returnValue.getSucceeded()) {
            getTaskIdList().addAll(returnValue.getInternalVdsmTaskIdList());
            return returnValue.getActionReturnValue();
        } else {
            throw new EngineException(returnValue.getFault().getError(),
                    String.format("Failed to create disk! %s", disk.getDiskAlias()));
        }
    }

    private AddDiskParameters buildAddDiskParameters(DiskImage disk) {
        AddDiskParameters parameters = new AddDiskParameters(Guid.Empty, disk);
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setShouldBeLogged(false);
        return parameters;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.createVolume;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_SUSPEND_VM : AuditLogType.USER_FAILED_SUSPEND_VM;
        case END_SUCCESS:
            if (getSucceeded()) {
                // no event should be displayed if the command ended successfully, the monitoring will log it
                return AuditLogType.UNASSIGNED;
            }
        case END_FAILURE:
        default:
            return isHibernateVdsProblematic ? AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE_WILL_TRY_AGAIN
                    : AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE;
        }
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!FeatureSupported.isSuspendSupportedByArchitecture(getVm().getClusterArch(),
                getVm().getVdsGroupCompatibilityVersion())) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_SUSPEND_NOT_SUPPORTED);
        }

        if (!canRunActionOnNonManagedVm()) {
           return false;
        }

        VMStatus vmStatus = getVm().getStatus();
        if (vmStatus == VMStatus.WaitForLaunch || vmStatus == VMStatus.NotResponding) {
            return failVmStatusIllegal();
        }

        if (vmStatus != VMStatus.Up) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_UP);
        }

        if (CommandCoordinatorUtil.entityHasTasks(getVmId())) {
            return failCanDoAction(EngineMessage.VM_CANNOT_SUSPENDE_HAS_RUNNING_TASKS);
        }

        if (getVm().getVmPoolId() != null) {
            return failCanDoAction(EngineMessage.VM_CANNOT_SUSPEND_VM_FROM_POOL);
        }

        // check if vm has stateless images in db in case vm was run once as stateless
        // (then isStateless is false)
        if (getVm().isStateless() ||
                DbFacade.getInstance().getSnapshotDao().exists(getVmId(), SnapshotType.STATELESS)) {
            return failCanDoAction(EngineMessage.VM_CANNOT_SUSPEND_STATELESS_VM);
        }

        if (getStorageDomainId() == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__TYPE__VM);
        addCanDoActionMessage(EngineMessage.VAR__ACTION__HIBERNATE);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getVmIsHibernatingMessage()));
    }

    private String getVmIsHibernatingMessage() {
        StringBuilder builder = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_VM_IS_HIBERNATING.name());
        if (getVmName() != null) {
            builder.append(String.format("$VmName %1$s", getVmName()));
        }
        return builder.toString();
    }

    @Override
    protected void endSuccessfully() {
        endActionOnDisks();
        if (getVm().getRunOnVds() == null) {
            log.warn(
                    "VM '{}' doesn't have 'run_on_vds' value - cannot Hibernate.",
                    getVm().getName());
            getReturnValue().setEndActionTryAgain(false);
        }

        else {
            String hiberVol = getActiveSnapshot().getMemoryVolume();
            if (hiberVol != null) {
                try {
                    runVdsCommand(VDSCommandType.Hibernate,
                            new HibernateVDSCommandParameters(getVm().getRunOnVds(), getVmId(), hiberVol));
                } catch (EngineException e) {
                    isHibernateVdsProblematic = true;
                    throw e;
                }
                setSucceeded(true);
            } else {
                log.error("Hibernation volume of VM '{}', is not initialized.", getVm().getName());
                endWithFailure();
            }
        }
    }

    @Override
    protected void endWithFailure() {
        endActionOnDisks();
        revertTasks();
        if (getVm().getRunOnVds() != null) {
            getSnapshotDao().removeMemoryFromActiveSnapshot(getVmId());
            setSucceeded(true);
        }

        else {
            log.warn(
                    "VM '{}' doesn't have 'run_on_vds' value - not clearing 'hibernation_vol_handle' info.",
                    getVm().getName());

            getReturnValue().setEndActionTryAgain(false);
        }
    }
}
