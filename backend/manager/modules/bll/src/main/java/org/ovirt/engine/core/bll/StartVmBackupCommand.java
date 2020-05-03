package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskExistenceValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmBackupPhase;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmBackupInfo;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class StartVmBackupCommand<T extends VmBackupParameters> extends VmCommand<T>
        implements SerialChildExecutingCommand {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private DiskDao diskDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmBackupDao vmBackupDao;
    @Inject
    private VmCheckpointDao vmCheckpointDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    private List<DiskImage> disksList;

    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public StartVmBackupCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void init() {
        setVmId(getParameters().getVmBackup().getVmId());
        setVdsId(getVm().getRunOnVds());
    }

    @Override
    protected boolean validate() {
        DiskExistenceValidator diskExistenceValidator = createDiskExistenceValidator(getDiskIds());
        if (!validate(diskExistenceValidator.disksNotExist())) {
            return false;
        }

        // validate all disks support incremental backup
        if (getParameters().getVmBackup().getFromCheckpointId() != null) {
            if (!FeatureSupported.isIncrementalBackupSupported(getCluster().getCompatibilityVersion())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_NOT_SUPPORTED);
            }

            DiskImagesValidator diskImagesValidator = createDiskImagesValidator(getDisks());
            if (!validate(diskImagesValidator.incrementalBackupEnabled())) {
                return false;
            }

            // Due to bz #1829829, Libvirt doesn't handle the case of mixing full and incremental
            // backup under the same operation. This situation can happen when adding a new disk
            // to a VM that already has a previous backup.
            Set<Guid> diskIds = getDisksNotInPreviousCheckpoint();
            if (!diskIds.isEmpty()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_MIXED_INCREMENTAL_AND_FULL_BACKUP_NOT_SUPPORTED,
                        String.format("$diskIds %s", diskIds));
            }
        }

        if (!getVm().getStatus().isQualifiedForVmBackup()) {
            return failValidation(EngineMessage.CANNOT_START_BACKUP_VM_SHOULD_BE_IN_UP_STATUS);
        }
        if (!vmBackupDao.getAllForVm(getVmId()).isEmpty()) {
            return failValidation(EngineMessage.CANNOT_START_BACKUP_ALREADY_IN_PROGRESS);
        }
        if (!getVds().isBackupEnabled()) {
            return failValidation(EngineMessage.CANNOT_START_BACKUP_NOT_SUPPORTED_BY_VDS,
                    String.format("$vdsName %s", getVdsName()));
        }
        return true;
    }

    public Set<Guid> getDisksNotInPreviousCheckpoint() {
        List<DiskImage> checkpointDisks =
                vmCheckpointDao.getDisksByCheckpointId(getParameters().getVmBackup().getFromCheckpointId());

        Set<Guid> vmCheckpointDisksIds = checkpointDisks
                .stream()
                .map(DiskImage::getId)
                .collect(Collectors.toCollection(HashSet::new));

        return getDiskIds().stream()
                .filter(diskId -> !vmCheckpointDisksIds.contains(diskId))
                .collect(Collectors.toSet());
    }

    @Override
    protected void executeCommand() {
        VmBackup vmBackup = getParameters().getVmBackup();

        // sets the backup disks with the disks from the DB
        // that contain all disk image data
        vmBackup.setDisks(getDisks());

        log.info("Creating VmBackup entity for VM '{}'", vmBackup.getVmId());
        Guid vmBackupId = createVmBackup();
        log.info("Created VmBackup entity '{}'", vmBackupId);

        // TODO: currently skip redefining backup checkpoints.
        // Will allow creating a full backup for a vm.
        // Redefine checkpoints should be implemented and used when the
        // API between the engine and vdsm will be re-designed.
        //
        // log.info("Redefine previous VM checkpoints for VM '{}'", vmId);
        // if (!redefineVmCheckpoints()) {
        //     setCommandStatus(CommandStatus.FAILED);
        //     return;
        // }
        // log.info("Successfully redefined previous VM checkpoints for VM '{}'", vmId);

        if (FeatureSupported.isIncrementalBackupSupported(getCluster().getCompatibilityVersion())
                && !isBackupContainsRawDisksOnly()) {
            log.info("Creating VmCheckpoint entity for VM '{}'", vmBackup.getVmId());
            Guid vmCheckpointId = createVmCheckpoint();
            log.info("Created VmCheckpoint entity '{}'", vmCheckpointId);

            // Set the the created checkpoint ID only in the parameters and not in the
            // VM backup DB entity. The VM backup DB entity will be updated once the
            // checkpoint will be created by the host.
            getParameters().setToCheckpointId(vmCheckpointId);
        } else {
            log.info("Skip checkpoint creation for VM '{}'", vmBackup.getVmId());
        }

        updateVmBackupPhase(VmBackupPhase.STARTING);
        persistCommandIfNeeded();
        setActionReturnValue(vmBackupId);
        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        restoreCommandState();

        switch (getParameters().getVmBackup().getPhase()) {
            case STARTING:
                if (runVmBackup()) {
                    updateVmBackupPhase(VmBackupPhase.READY);
                    log.info("Ready to start image transfers using backup URLs");
                } else {
                    setCommandStatus(CommandStatus.FAILED);
                }
                break;

            case READY:
                return true;

            case FINALIZING:
                finalizeVmBackup();
                setCommandStatus(CommandStatus.SUCCEEDED);
                break;
        }
        persistCommandIfNeeded();
        return true;
    }

    private boolean runVmBackup() {
        lockDisks();
        VmBackupInfo vmBackupInfo = null;
        if (!getParameters().isBackupInitiated()) {
            getParameters().setBackupInitiated(true);
            persistCommandIfNeeded();
            vmBackupInfo = performVmBackupOperation(VDSCommandType.StartVmBackup);
        }

        if (vmBackupInfo == null || vmBackupInfo.getDisks() == null) {
            // Check if backup already started at the host
            if (!getParameters().isBackupInitiated()) {
                // backup operation didn't start yet, fail the operation
                return false;
            }

            vmBackupInfo = recoverFromMissingBackupInfo();
            if (vmBackupInfo == null) {
                return false;
            }
        } else if (vmBackupInfo.getCheckpoint() == null && !isBackupContainsRawDisksOnly()
                && FeatureSupported.isIncrementalBackupSupported(getCluster().getCompatibilityVersion())) {
            vmBackupInfo = recoverFromMissingCheckpointInfo();
            if (vmBackupInfo == null) {
                return false;
            }
        }

        if (vmBackupInfo.getCheckpoint() != null) {
            updateVmBackupCheckpoint(vmBackupInfo);
        }
        storeBackupsUrls(vmBackupInfo.getDisks());
        return true;
    }

    private VmBackupInfo recoverFromMissingBackupInfo() {
        // Try to recover by fetching the backup info
        VmBackupInfo vmBackupInfo = performVmBackupOperation(VDSCommandType.GetVmBackupInfo);
        if (vmBackupInfo == null || vmBackupInfo.getDisks() == null) {
            log.error("Failed to start VM '{}' backup '{}' on the host",
                    getVmId(),
                    getParameters().getVmBackup().getId());
            return null;
        }
        return vmBackupInfo;
    }

    private VmBackupInfo recoverFromMissingCheckpointInfo() {
        // Try to fetch the checkpoint XML again
        VmBackupInfo vmBackupInfo = performVmBackupOperation(VDSCommandType.GetVmBackupInfo);
        if (vmBackupInfo == null || vmBackupInfo.getCheckpoint() == null) {
            // Best effort - stop the backup
            runInternalAction(ActionType.StopVmBackup, getParameters());
            auditLogDirector.log(this, AuditLogType.VM_BACKUP_STOPPED);
            log.error("Failed to fetch checkpoint id: '{}' XML from Libvirt, VM id: '{}' "
                            + "backup chain cannot be used anymore and a full backup should be taken.",
                    getParameters().getVmBackup().getToCheckpointId(), getVmId());
            return null;
        }
        return vmBackupInfo;
    }

    private void finalizeVmBackup() {
        vmBackupDao.removeAllDisksFromBackup(getParameters().getVmBackup().getId());
        unlockDisks();
    }

    private void removeCheckpointFromDb() {
        Guid vmCheckpointId = getParameters().getVmBackup().getToCheckpointId();
        log.info("Remove VmCheckpoint entity '{}'", vmCheckpointId);

        TransactionSupport.executeInNewTransaction(() -> {
            vmCheckpointDao.removeAllDisksFromCheckpoint(vmCheckpointId);
            vmCheckpointDao.remove(vmCheckpointId);
            return null;
        });
    }

    private Guid createVmBackup() {
        final VmBackup vmBackup = getParameters().getVmBackup();
        vmBackup.setId(getCommandId());
        vmBackup.setPhase(VmBackupPhase.INITIALIZING);
        vmBackup.setCreationDate(new Date());
        getParameters().setVmBackup(vmBackup);
        TransactionSupport.executeInNewTransaction(() -> {
            vmBackupDao.save(vmBackup);
            getParameters().getVmBackup().getDisks().forEach(
                    disk -> vmBackupDao.addDiskToVmBackup(vmBackup.getId(), disk.getId()));
            return null;
        });
        persistCommandIfNeeded();
        return vmBackup.getId();
    }

    private Guid createVmCheckpoint() {
        final VmCheckpoint vmCheckpoint = new VmCheckpoint();
        vmCheckpoint.setId(Guid.newGuid());
        vmCheckpoint.setParentId(getParameters().getVmBackup().getFromCheckpointId());
        vmCheckpoint.setVmId(getParameters().getVmBackup().getVmId());
        vmCheckpoint.setCreationDate(new Date());

        TransactionSupport.executeInNewTransaction(() -> {
            vmCheckpointDao.save(vmCheckpoint);
            getParameters().getVmBackup().getDisks().stream()
                    .filter(DiskImage::isQcowFormat)
                    .forEach(disk -> vmCheckpointDao.addDiskToCheckpoint(vmCheckpoint.getId(), disk.getId()));
            return null;
        });
        persistCommandIfNeeded();
        return vmCheckpoint.getId();
    }

    private boolean isBackupContainsRawDisksOnly() {
        return getParameters().getVmBackup()
                .getDisks()
                .stream()
                .noneMatch(DiskImage::isQcowFormat);
    }

    private boolean redefineVmCheckpoints() {
        VDSReturnValue vdsRetVal;
        try {
            List<VmCheckpoint> checkpoints = vmCheckpointDao.getAllForVm(getVmId());
            if (checkpoints.isEmpty()) {
                log.info("No previous VM checkpoints found for VM '{}', skip redefine VM checkpoints", getVmId());
                return true;
            }

            checkpoints.forEach(c -> c.setDisks(vmCheckpointDao.getDisksByCheckpointId(c.getId())));
            vdsRetVal = runVdsCommand(VDSCommandType.RedefineVmCheckpoints,
                    new VmCheckpointsVDSParameters(getVdsId(), getVmId(), checkpoints));
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                throw engineException;
            }
            return true;
        } catch (EngineException e) {
            log.error("Failed to execute VM.redefineCheckpoints: {}", e);
            return false;
        }
    }

    private VmBackupInfo performVmBackupOperation(VDSCommandType vdsCommandType) {
        VDSReturnValue vdsRetVal;
        // Add the created checkpoint ID
        VmBackup vmBackup = getParameters().getVmBackup();
        vmBackup.setToCheckpointId(getParameters().getToCheckpointId());
        try {
            vdsRetVal = runVdsCommand(vdsCommandType, new VmBackupVDSParameters(getVdsId(), vmBackup));
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                throw engineException;
            }
            VmBackupInfo vmBackupInfo = (VmBackupInfo) vdsRetVal.getReturnValue();
            return vmBackupInfo;
        } catch (EngineException e) {
            log.error("Failed to execute VM backup operation '{}': {}", vdsCommandType, e);
            return null;
        }
    }

    private void storeBackupsUrls(Map<String, Object> disks) {
        disks.keySet().forEach(diskId ->
                vmBackupDao.addBackupUrlToVmBackup(getParameters().getVmBackup().getId(),
                        Guid.createGuidFromString(diskId),
                        (String) disks.get(diskId)));
    }

    private void restoreCommandState() {
        getParameters().setVmBackup(vmBackupDao.get(getParameters().getVmBackup().getId()));
        getParameters().getVmBackup().setDisks(
                vmBackupDao.getDisksByBackupId(getParameters().getVmBackup().getId()));
    }

    private void updateVmBackupPhase(VmBackupPhase phase) {
        getParameters().getVmBackup().setPhase(phase);
        vmBackupDao.update(getParameters().getVmBackup());
    }

    private void updateVmBackupCheckpoint(VmBackupInfo vmBackupInfo) {
        TransactionSupport.executeInNewTransaction(() -> {
            // Update the VmBackup to include the checkpoint ID
            vmBackupDao.update(getParameters().getVmBackup());
            // Update the vmCheckpoint to include the checkpoint XML
            vmCheckpointDao.updateCheckpointXml(getParameters().getVmBackup().getToCheckpointId(),
                    vmBackupInfo.getCheckpoint());
            return null;
        });
    }

     @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        finalizeVmBackup();
        removeCheckpointFromDb();
        getReturnValue().setEndActionTryAgain(false);
        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__BACKUP);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        getParameters().getVmBackup().getDisks().forEach(
                disk -> permissionList.add(
                        new PermissionSubject(disk.getId(), VdcObjectType.Disk, ActionGroup.BACKUP_DISK)));
        return permissionList;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getParameters().getVmBackup().getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED));
        return locks;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        getDiskIds().forEach(id -> locks.put(id.toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_DISK_IS_LOCKED)));
        return locks;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("VmName", getVm().getName());
        switch (getActionState()) {
            case EXECUTE:
                return AuditLogType.VM_BACKUP_STARTED;
            case END_FAILURE:
                return AuditLogType.VM_BACKUP_FAILED;
            case END_SUCCESS:
                if (!getSucceeded()) {
                    return AuditLogType.VM_BACKUP_FAILED;
                }
                if (getParameters().getVmBackup().getPhase() == VmBackupPhase.FINALIZING) {
                    return AuditLogType.VM_BACKUP_SUCCEEDED;
                }
        }
        return null;
    }

    private void lockDisks() {
        imagesHandler.updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(
                getDiskIds(),
                ImageStatus.LOCKED,
                ImageStatus.OK,
                getCompensationContext());
    }

    private void unlockDisks() {
        imagesHandler.updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(
                getDiskIds(),
                ImageStatus.OK,
                ImageStatus.ILLEGAL,
                getCompensationContext());
    }

    protected DiskExistenceValidator createDiskExistenceValidator(Set<Guid> disksGuids) {
        return Injector.injectMembers(new DiskExistenceValidator(disksGuids));
    }

    protected DiskImagesValidator createDiskImagesValidator(List<DiskImage> disks) {
        return Injector.injectMembers(new DiskImagesValidator(disks));
    }

    public Set<Guid> getDiskIds() {
        return getParameters().getVmBackup().getDisks() == null ? Collections.emptySet() :
                getParameters().getVmBackup().getDisks().stream().map(DiskImage::getId).collect(
                        Collectors.toCollection(LinkedHashSet::new));
    }

    private List<DiskImage> getDisks() {
        if (disksList == null) {
            List<Disk> vmDisks = diskDao.getAllForVm(getVmId());
            List<DiskImage> diskImages = DisksFilter.filterImageDisks(vmDisks, ONLY_NOT_SHAREABLE,
                    ONLY_SNAPABLE, ONLY_ACTIVE);
            disksList = diskImages.stream().filter(d -> getDiskIds().contains(d.getId())).collect(Collectors.toList());
        }
        return disksList;
    }

}
