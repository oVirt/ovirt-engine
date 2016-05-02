package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.VmUtils;
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
@NonTransactiveCommandAttribute
public class HibernateVmCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {
    private static final Logger log = LoggerFactory.getLogger(HibernateVmCommand.class);

    private boolean hibernateVdsProblematic;
    private Guid cachedStorageDomainId;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected HibernateVmCommand(Guid commandId) {
        super(commandId);
    }

    public HibernateVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
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
                    VmUtils.getSnapshotMemorySizeInBytes(getVm()),
                    MemoryUtils.METADATA_SIZE_IN_BYTES);
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
        addMemoryDisk();
        addMetadataDisk();
        setSucceeded(true);
    }

    private void addMetadataDisk() {
        DiskImage metaDataDisk = MemoryUtils.createHibernationMetadataDisk(getVm());
        addDisk(metaDataDisk);
    }

    private void addMemoryDisk() {
        DiskImage memoryDisk = MemoryUtils.createHibernationMemoryDisk(getVm(), getStorageDomain().getStorageType());
        addDisk(memoryDisk);
    }

    private void addDisk(DiskImage disk) {
        VdcReturnValueBase returnValue = runInternalActionWithTasksContext(
                VdcActionType.AddDisk,
                buildAddDiskParameters(disk));

        if (!returnValue.getSucceeded()) {
            throw new EngineException(returnValue.getFault().getError(),
                    String.format("Failed to create disk! %s", disk.getDiskAlias()));
        }

        getTaskIdList().addAll(returnValue.getInternalVdsmTaskIdList());
    }

    private AddDiskParameters buildAddDiskParameters(DiskImage disk) {
        AddDiskParameters parameters = new AddDiskParameters(disk);
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
            return hibernateVdsProblematic ? AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE_WILL_TRY_AGAIN
                    : AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE;
        }
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!FeatureSupported.isSuspendSupportedByArchitecture(
                getVm().getClusterArch(),
                getVm().getCompatibilityVersion())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SUSPEND_NOT_SUPPORTED);
        }

        if (!canRunActionOnNonManagedVm()) {
           return false;
        }

        VMStatus vmStatus = getVm().getStatus();
        if (vmStatus == VMStatus.WaitForLaunch || vmStatus == VMStatus.NotResponding) {
            return failVmStatusIllegal();
        }

        if (vmStatus != VMStatus.Up) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_UP);
        }

        if (getCluster().isInUpgradeMode()) {
            return failValidation(EngineMessage.VM_CANNOT_SUSPEND_CLUSTER_UPGRADING);
        }

        if (CommandCoordinatorUtil.entityHasTasks(getVmId())) {
            return failValidation(EngineMessage.VM_CANNOT_SUSPENDE_HAS_RUNNING_TASKS);
        }

        if (getVm().getVmPoolId() != null) {
            return failValidation(EngineMessage.VM_CANNOT_SUSPEND_VM_FROM_POOL);
        }

        // check if vm has stateless images in db in case vm was run once as stateless
        // (then isStateless is false)
        if (getVm().isStateless() ||
                DbFacade.getInstance().getSnapshotDao().exists(getVmId(), SnapshotType.STATELESS)) {
            return failValidation(EngineMessage.VM_CANNOT_SUSPEND_STATELESS_VM);
        }

        if (getStorageDomainId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
        addValidationMessage(EngineMessage.VAR__ACTION__HIBERNATE);
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
        if (getVm().getStatus() != VMStatus.Up) {
            log.warn("VM '{}' is not up, cannot Hibernate.", getVm().getName());
            endWithFailure();
            return;
        }

        List<VdcReturnValueBase> addDiskReturnValues = endActionOnDisks();
        DiskImage dumpDisk = getMemoryDumpDisk(addDiskReturnValues);
        DiskImage metadataDisk = getMemoryMetadataDisk(addDiskReturnValues);

        String hiberVol = MemoryUtils.createMemoryStateString(
                getStorageDomainId(), getStoragePoolId(),
                dumpDisk.getId(), dumpDisk.getImageId(), metadataDisk.getId(), metadataDisk.getImageId());

        try {
            runVdsCommand(VDSCommandType.Hibernate,
                    new HibernateVDSCommandParameters(getVm().getRunOnVds(), getVmId(), hiberVol));
            getSnapshotDao().updateHibernationMemory(getVmId(),
                    dumpDisk.getId(), metadataDisk.getId(), hiberVol);
        } catch (EngineException e) {
            hibernateVdsProblematic = true;
            endWithFailure();
            return;
        }

        setSucceeded(true);
    }

    private DiskImage getMemoryDumpDisk(List<VdcReturnValueBase> returnValues) {
        for (VdcReturnValueBase returnValue : returnValues) {
            DiskImage disk = returnValue.getActionReturnValue();
            if (disk.getSize() != MemoryUtils.METADATA_SIZE_IN_BYTES) {
                return disk;
            }
        }

        return null;
    }

    private DiskImage getMemoryMetadataDisk(List<VdcReturnValueBase> returnValues) {
        for (VdcReturnValueBase returnValue : returnValues) {
            DiskImage disk = returnValue.getActionReturnValue();
            if (disk.getSize() == MemoryUtils.METADATA_SIZE_IN_BYTES) {
                return disk;
            }
        }

        return null;
    }

    @Override
    protected void endWithFailure() {
        endActionOnDisks();
        getSnapshotDao().removeMemoryFromActiveSnapshot(getVmId());
        revertTasks();
        setSucceeded(true);
    }
}
