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
        DiskImagesValidator diskImagesValidator = createDiskImagesValidator(getDisks());
        if (!validate(diskImagesValidator.incrementalBackupEnabled())) {
            return false;
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
        Guid vmId = getParameters().getVmBackup().getVmId();

        log.info("Creating VmBackup entity for VM '{}'", vmId);
        Guid vmBackupId = createVmBackup();
        log.info("Created VmBackup entity '{}'", vmBackupId);

        log.info("Redefine previous VM checkpoints for VM '{}'", vmId);
        if (!redefineVmCheckpoints()) {
            setCommandStatus(CommandStatus.FAILED);
            return;
        }
        log.info("Successfully redefined previous VM checkpoints for VM '{}'", vmId);

        log.info("Creating VmCheckpoint entity for VM '{}'", vmId);
        Guid vmCheckpointId = createVmCheckpoint();
        log.info("Created VmCheckpoint entity '{}'", vmCheckpointId);

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
                Map<String, Object> disks = startVmBackup();
                if (disks != null) {
                    storeBackupsUrls(disks);
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

        final List<VmCheckpoint> checkpoints = vmCheckpointDao.getAllForVm(getVmId());
        if (!checkpoints.isEmpty()) {
            vmCheckpoint.setParentId(checkpoints.get(checkpoints.size() - 1).getId());
        }

        TransactionSupport.executeInNewTransaction(() -> {
            vmCheckpointDao.save(vmCheckpoint);
            getParameters().getVmBackup().getDisks().forEach(
                    disk -> vmCheckpointDao.addDiskToCheckpoint(vmCheckpoint.getId(), disk.getId()));
            return null;
        });
        persistCommandIfNeeded();
        return vmCheckpoint.getId();
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

    private Map<String, Object> startVmBackup() {
        VDSReturnValue vdsRetVal;
        try {
            vdsRetVal = runVdsCommand(VDSCommandType.StartVmBackup,
                    new VmBackupVDSParameters(getVdsId(), getParameters().getVmBackup()));
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                throw engineException;
            }
            return (Map<String, Object>) vdsRetVal.getReturnValue();
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
        List<Disk> disks = diskDao.getAllForVm(getVmId());
        List<DiskImage> diskImages = DisksFilter.filterImageDisks(disks, ONLY_NOT_SHAREABLE,
                ONLY_SNAPABLE, ONLY_ACTIVE);
        diskImages = diskImages.stream().filter(d -> getDiskIds().contains(d.getId())).collect(Collectors.toList());
        return diskImages;
    }

}
