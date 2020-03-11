package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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

            // Update the VmBackup to include the checkpoint ID.
            vmBackup.setToCheckpointId(vmCheckpointId);
            TransactionSupport.executeInNewTransaction(() -> {
                vmBackupDao.update(vmBackup);
                return null;
            });
        } else {
            log.info("Skip checkpoint creation for VM '{}'", vmBackup.getVmId());
        }

        persistCommandIfNeeded();
        setActionReturnValue(vmBackupId);
        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        restoreCommandState();

        switch (getParameters().getVmBackup().getPhase()) {
            case INITIALIZING:
                updateVmBackupPhase(VmBackupPhase.STARTING);
                break;

            case STARTING:
                updateVmBackupPhase(VmBackupPhase.READY);
                break;

            case READY:
                return true;
        }
        persistCommandIfNeeded();
        executeNextOperation();
        return true;
    }

    private void executeNextOperation() {
        switch (getParameters().getVmBackup().getPhase()) {
            case STARTING:
                lockDisks();
                VmBackupInfo vmBackupInfo = startVmBackup();
                if (vmBackupInfo != null && vmBackupInfo.getDisks() != null) {
                    if (vmBackupInfo.getCheckpoint() != null) {
                        vmCheckpointDao.updateCheckpointXml(getParameters().getVmBackup().getToCheckpointId(),
                                vmBackupInfo.getCheckpoint());
                    }
                    storeBackupsUrls(vmBackupInfo.getDisks());
                } else {
                    setCommandStatus(CommandStatus.FAILED);
                }
                break;

            case READY:
                log.info("Ready to start image transfers using backup URLs");
                break;

            case FINALIZING:
                finalizeVmBackup();
                setCommandStatus(CommandStatus.SUCCEEDED);
                break;
        }
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

    private VmBackupInfo startVmBackup() {
        VDSReturnValue vdsRetVal;
        try {
            vdsRetVal = runVdsCommand(VDSCommandType.StartVmBackup,
                    new VmBackupVDSParameters(getVdsId(), getParameters().getVmBackup()));
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                throw engineException;
            }
            VmBackupInfo vmBackupInfo = (VmBackupInfo) vdsRetVal.getReturnValue();
            return vmBackupInfo;
        } catch (EngineException e) {
            log.error("Failed to execute VM.startBackup: {}", e);
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
