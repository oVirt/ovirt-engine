package org.ovirt.engine.core.bll.storage.backup;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskExistenceValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DeleteAllVmCheckpointsParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.action.VolumeBitmapCommandParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmBackupPhase;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.VmCheckpointState;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackupMode;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.Qcow2BitmapInfo;
import org.ovirt.engine.core.common.businessentities.storage.QemuImageInfo;
import org.ovirt.engine.core.common.businessentities.storage.VmBackupType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ScratchDiskInfo;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmBackupInfo;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;

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
    private BaseDiskDao baseDiskDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    private List<DiskImage> disksList;
    private VmCheckpoint vmCheckpointsLeaf;
    private Set<Guid> fromCheckpointDisksIds;
    private boolean isLiveBackup;

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
        // BZ#1989121: Blocking HostedEngine VM backup
        if (getVm().isHostedEngine()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_VM_BACKUP_NOT_SUPPORTED);
        }

        DiskExistenceValidator diskExistenceValidator = createDiskExistenceValidator(getDiskIds());
        if (!validate(diskExistenceValidator.disksNotExist())) {
            return false;
        }

        DiskImagesValidator diskImagesValidator = createDiskImagesValidator(getDisks());
        if (!validate(diskImagesValidator.diskImagesNotLocked())) {
            return false;
        }

        if (!validate(allDisksPlugged())) {
            return false;
        }

        VmBackup vmBackup = getParameters().getVmBackup();
        if (vmBackup.getFromCheckpointId() != null) {
            if (!FeatureSupported.isIncrementalBackupSupported(getCluster().getCompatibilityVersion())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_NOT_SUPPORTED);
            }

            VmCheckpoint fromCheckpoint = vmCheckpointDao.get(vmBackup.getFromCheckpointId());
            if (fromCheckpoint == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CHECKPOINT_NOT_EXIST,
                        String.format("$checkpointId %s", vmBackup.getFromCheckpointId()));
            }

            if (fromCheckpoint.getState().equals(VmCheckpointState.INVALID)) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CHECKPOINT_INVALID,
                        String.format("$checkpointId %s", vmBackup.getFromCheckpointId()));
            }

            if (!FeatureSupported.isBackupModeAndBitmapsOperationsSupported(getCluster().getCompatibilityVersion())) {
                // Due to bz #1829829, Libvirt doesn't handle the case of mixing full and incremental
                // backup under the same operation. This situation can happen when adding a new disk
                // to a VM that already has a previous backup or when RAW disks are part of the backup.
                if (!validate(diskImagesValidator.incrementalBackupEnabled())) {
                    return false;
                }

                Set<Guid> diskIds = getDisksNotInPreviousCheckpoint();
                if (!diskIds.isEmpty()) {
                    return failValidation(
                            EngineMessage.ACTION_TYPE_FAILED_MIXED_INCREMENTAL_AND_FULL_BACKUP_NOT_SUPPORTED,
                            String.format("$diskIds %s", diskIds));
                }
            }
        }

        if (!getVm().getStatus().isQualifiedForVmBackup()) {
            return failValidation(EngineMessage.CANNOT_START_BACKUP_VM_SHOULD_BE_IN_UP_OR_DOWN_STATUS);
        }
        // Sets the VM backup type (live/cold) according to the VM status.
        isLiveBackup = getVm().getStatus().equals(VMStatus.Up);

        if (!validate(snapshotsValidator.vmNotInPreview(getVm().getId()))) {
            return false;
        }

        if (isLiveBackup) {
            // Validate that the host supports building checkpoint XML for redefinition
            // and creating scratch disks on shared storage.
            // Those features were introduced together with the support for cold backup so if the
            // host supports cold backup it supports scratch disks on shared storage also.
            if (!getVds().isColdBackupEnabled()) {
                return failValidation(
                        EngineMessage.CANNOT_START_BACKUP_USING_OUTDATED_HOST,
                        String.format("$vdsName %s", getVdsName()));
            }

            if (!getVds().isBackupEnabled()) {
                return failValidation(EngineMessage.CANNOT_START_BACKUP_NOT_SUPPORTED_BY_VDS,
                        String.format("$vdsName %s", getVdsName()));
            }
        } else {
            if (!FeatureSupported.isIncrementalBackupSupported(getCluster().getCompatibilityVersion())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_NOT_SUPPORTED);
            }
            if (!FeatureSupported.isBackupModeAndBitmapsOperationsSupported(getCluster().getCompatibilityVersion())) {
                return failValidation(EngineMessage.ACTION_TYPE_BITMAPS_OPERATION_ARE_NOT_SUPPORTED);
            }
        }
        if (isVmDuringBackup()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_BACKUP);
        }

        if (vmBackup.getDescription() != null && vmBackup.getDescription().length() > 1024) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_BACKUP_DESCRIPTION_IS_TOO_LONG);
        }

        if (vmBackup.getId() != null && vmBackupDao.get(vmBackup.getId()) != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_BACKUP_ID_ALREADY_EXIST,
                    String.format("$backupId %s", vmBackup.getId()));
        }
        return true;
    }

    public Set<Guid> getDisksNotInPreviousCheckpoint() {
        return getDiskIds()
                .stream()
                .filter(diskId ->
                        !getFromCheckpointDisksIds(getParameters().getVmBackup().getFromCheckpointId()).contains(diskId))
                .collect(Collectors.toSet());
    }

    public ValidationResult allDisksPlugged() {
        List<Guid> unpluggedDisks = getParameters().getVmBackup().getDisks()
                .stream()
                .map(diskImage -> vmDeviceDao.get(new VmDeviceId(diskImage.getId(), getVmId())))
                .filter(vmDevice -> !vmDevice.isPlugged())
                .map(VmDevice::getDeviceId)
                .collect(Collectors.toList());
        if (!unpluggedDisks.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_ARE_NOT_ACTIVE,
                    ReplacementUtils.createSetVariableString("vmName", getVm().getName()),
                    ReplacementUtils.createSetVariableString("diskIds", StringUtils.join(unpluggedDisks, ", ")));
        }
        return ValidationResult.VALID;
    }

    @Override
    protected void executeCommand() {
        lockDisks();
        VmBackup vmBackup = getParameters().getVmBackup();

        // Sets the backup disks with the disks from the DB that contain all disk image data.
        vmBackup.setDisks(getDisks());

        log.info("Creating VmBackup entity for VM '{}'", vmBackup.getVmId());
        Guid vmBackupId = createVmBackup();
        log.info("Created VmBackup entity '{}'", vmBackupId);

        // Set a VDS to be able to gather Qemu Image Info
        if (getVds() == null) {
            setHostForColdBackupOperation();
        }
        if (getVds().isQemuImageInfoBitmaps()) {
            log.info("Checking VM checkpoint '{}' for VM '{}'", vmBackup.getFromCheckpointId(), vmBackup.getVmId());
            if (!validateCheckpoint(vmBackup.getFromCheckpointId())) {
                addCustomValue("backupId", vmBackupId.toString());
                auditLogDirector.log(this, AuditLogType.VM_INCREMENTAL_BACKUP_FAILED_FULL_VM_BACKUP_NEEDED);
                setCommandStatus(CommandStatus.FAILED);
                return;
            }
            log.info("Previous VM checkpoint '{}' for VM '{}' is valid", vmBackup.getFromCheckpointId(), vmBackup.getVmId());
        } else {
            log.info("Could not check VM checkpoint '{}' for VM '{}' due to missing bitmap info support in vdsm",
                     vmBackup.getFromCheckpointId(), vmBackup.getVmId());
        }

        if (isLiveBackup()) {
            log.info("Redefine previous VM checkpoints for VM '{}'", vmBackup.getVmId());
            if (!redefineVmCheckpoints()) {
                addCustomValue("backupId", vmBackupId.toString());
                auditLogDirector.log(this, AuditLogType.VM_INCREMENTAL_BACKUP_FAILED_FULL_VM_BACKUP_NEEDED);
                setCommandStatus(CommandStatus.FAILED);
                return;
            }
            log.info("Successfully redefined previous VM checkpoints for VM '{}'", vmBackup.getVmId());
        }

        if (FeatureSupported.isIncrementalBackupSupported(getCluster().getCompatibilityVersion())
                && !isBackupContainsRawDisksOnly()) {
            log.info("Creating VmCheckpoint entity for VM '{}'", vmBackup.getVmId());
            Guid vmCheckpointId = createVmCheckpoint(Guid.newGuid());
            log.info("Created VmCheckpoint entity '{}'", vmCheckpointId);

            // Set the the created checkpoint ID only in the parameters and not in the
            // VM backup DB entity. The VM backup DB entity will be updated once the
            // checkpoint will be created by the host.
            getParameters().setToCheckpointId(vmCheckpointId);
        } else {
            log.info("Skip checkpoint creation for VM '{}'", vmBackup.getVmId());
        }

        // For live VM backup, scratch disks should be created and prepared for each disk in the backup.
        // For cold VM backup, volume bitmaps need to be added
        VmBackupPhase nextPhase = isLiveBackup() ? VmBackupPhase.CREATING_SCRATCH_DISKS : VmBackupPhase.ADDING_BITMAPS;
        updateVmBackupPhase(nextPhase);

        persistCommandIfNeeded();
        setActionReturnValue(vmBackupId);
        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        try (EngineLock backupLock = getEntityUpdateLock(getParameters().getVmBackup().getId())) {
            lockManager.acquireLockWait(backupLock);

            restoreCommandState();

            switch (getParameters().getVmBackup().getPhase()) {
                case CREATING_SCRATCH_DISKS:
                    if (createScratchDisks()) {
                        updateVmBackupPhase(VmBackupPhase.STARTING);
                        log.info("Scratch disks created for the backup");
                    } else {
                        updateVmBackupPhase(VmBackupPhase.FINALIZING_FAILURE);
                    }
                    break;

                case ADDING_BITMAPS:
                    if (!startAddBitmapJobs()) {
                        updateVmBackupPhase(VmBackupPhase.FINALIZING_FAILURE);
                        break;
                    }

                    log.info("Waiting for add bitmaps jobs completion");
                    updateVmBackupPhase(VmBackupPhase.WAITING_FOR_BITMAPS);

                    break;

                case STARTING:
                    Map<Guid, ScratchDiskInfo> scratchDiskInfoMap = prepareScratchDisks();
                    if (scratchDiskInfoMap == null) {
                        updateVmBackupPhase(VmBackupPhase.FINALIZING_FAILURE);
                        break;
                    }
                    log.info("Scratch disks prepared for the backup");

                    if (!runLiveVmBackup(scratchDiskInfoMap)) {
                        updateVmBackupPhase(VmBackupPhase.FINALIZING_FAILURE);
                        break;
                    }

                    // Since live backup is a single synchronous VDS command we can set the backup
                    // phase to READY immediately
                    updateVmBackupPhase(VmBackupPhase.READY);
                    log.info("Ready to start image transfers");
                    break;

                case WAITING_FOR_BITMAPS:
                    updateVmBackupPhase(VmBackupPhase.READY);
                    log.info("Ready to start image transfers");

                    break;

                case READY:
                    return true;

                case FINALIZING:
                    setCommandStatus(CommandStatus.SUCCEEDED);
                    break;

                case FINALIZING_FAILURE:
                    setCommandStatus(CommandStatus.FAILED);
                    break;
            }
            persistCommandIfNeeded();
        }
        return true;
    }

    private boolean validateCheckpoint(Guid checkpointId) {
        List<DiskImage> images = vmCheckpointDao.getDisksByCheckpointId(checkpointId);
        /* Check if the checkpoint is still there on each volume/image */
        for (DiskImage image : images) {
            QemuImageInfo qcow2Info = imagesHandler.getQemuImageInfoFromVdsm(
                            getStoragePoolId(),
                            image.getStorageIds().get(0),
                            image.getId(),
                            image.getImageId(),
                            getParameters().getVdsRunningOn(),
                            !isLiveBackup());

            boolean valid = false;
            if (qcow2Info != null) {
                List<Qcow2BitmapInfo> bitmaps = qcow2Info.getQcow2bitmaps();
                if (bitmaps != null) {
                    valid = bitmaps.stream().anyMatch(bitmap -> bitmap.getName().equals(checkpointId));
                }
            }
            /* Bitmap did not exist on disk -> Remove checkpoints */
            if (!valid) {
                log.error("Checkpoint '{}' does not exist for disk '{}'. Removing checkpoints",
                        checkpointId,
                        image.getId());
                /* Some checkpoint corruption, remove checkpoints */
                DeleteAllVmCheckpointsParameters deleteAllVmCheckpointsParameters =
                new DeleteAllVmCheckpointsParameters(getVmId(), List.of(image));
                deleteAllVmCheckpointsParameters.setParentCommand(getActionType());
                deleteAllVmCheckpointsParameters.setParentParameters(getParameters());
                deleteAllVmCheckpointsParameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
                deleteAllVmCheckpointsParameters.setForce(true);

                runInternalAction(ActionType.DeleteAllVmCheckpoints, deleteAllVmCheckpointsParameters);
                return false;
            }
        }
        return true;
    }

    private boolean redefineVmCheckpoints() {
        VmBackupParameters parameters = new VmBackupParameters(getParameters().getVmBackup());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.PARENT_MANAGED);

        ActionReturnValue returnValue = runInternalAction(ActionType.RedefineVmCheckpoint, parameters,
                cloneContext().withoutLock());
        return returnValue != null && returnValue.getSucceeded();
    }

    private boolean startAddBitmapJobs() {
        VmBackup vmBackup = getParameters().getVmBackup();
        if (getParameters().getVdsRunningOn() == null) {
            log.error("Failed to find host to run cold backup operation for VM '{}'", vmBackup.getVmId());
            return false;
        }
        vmBackup.setToCheckpointId(getParameters().getToCheckpointId());

        for (DiskImage diskImage : vmBackup.getDisks()) {
            if (!diskImage.isQcowFormat()) {
                continue;
            }

            VdsmImageLocationInfo locationInfo = new VdsmImageLocationInfo(
                    diskImage.getStorageIds().get(0),
                    diskImage.getId(),
                    diskImage.getImageId(),
                    null);

            VolumeBitmapCommandParameters parameters =
                    new VolumeBitmapCommandParameters(
                            getStoragePoolId(),
                            locationInfo,
                            getParameters().getToCheckpointId().toString());
            parameters.setVdsId(getParameters().getVdsRunningOn());
            parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
            parameters.setParentCommand(getActionType());
            parameters.setParentParameters(getParameters());

            ActionReturnValue returnValue = runInternalActionWithTasksContext(ActionType.AddVolumeBitmap, parameters);
            if (!returnValue.getSucceeded()) {
                log.error("Failed to add bitmap to disk '{}'", diskImage.getId());
                return false;
            }
        }
        updateVmBackupCheckpoint();

        return true;
    }

    private void setHostForColdBackupOperation() {
        if (getParameters().getVdsRunningOn() == null) {
            getParameters().setVdsRunningOn(
                    vdsCommandsHelper.getHostForExecution(getStoragePoolId(), VDS::isColdBackupEnabled));
            persistCommandIfNeeded();
        }
    }

    private boolean runLiveVmBackup(Map<Guid, ScratchDiskInfo> scratchDiskInfoMap) {
        VmBackupInfo vmBackupInfo = null;
        if (!getParameters().isBackupInitiated()) {
            getParameters().setBackupInitiated(true);
            persistCommandIfNeeded();
            vmBackupInfo = performVmBackupOperation(VDSCommandType.StartVmBackup, scratchDiskInfoMap);
        }

        if (vmBackupInfo == null || vmBackupInfo.getDisks() == null) {
            // Check if backup already started at the host.
            if (!getParameters().isBackupInitiated()) {
                // Backup operation didn't start yet, fail the operation.
                log.error("Failed to initiate VM '{}' backup on the host", getVmId());
                return false;
            }

            // Try to recover from the missing backup info issue by trying to fetch it.
            vmBackupInfo = performVmBackupOperation(VDSCommandType.GetVmBackupInfo, scratchDiskInfoMap);
            if (vmBackupInfo == null || vmBackupInfo.getDisks() == null) {
                log.error("Failed to start VM '{}' backup '{}' on the host",
                        getVmId(),
                        getParameters().getVmBackup().getId());
                return false;
            }
        }

        updateVmBackupCheckpoint();
        storeBackupsUrls(vmBackupInfo.getDisks());
        return true;
    }

    private void finalizeVmBackup(VmBackupPhase phase) {
        cleanDisksBackupModeIfSupported();
        freeLock();
        unlockDisks();

        // Remove the created scratch disks.
        if (!getParameters().getScratchDisksMap().isEmpty()) {
            removeScratchDisks();
        }

        // At this point the lock taken by RemoveScratchDisksCommand should be released
        updateVmBackupPhase(phase);
    }

    private void removeScratchDisks() {
        // Best effort to teardown and remove the scratch disks.
        VmBackup vmBackup = getParameters().getVmBackup();
        log.info("Remove all the scratch disks that were created for backup '{}'", vmBackup.getId());
        VmBackupParameters parameters = new VmBackupParameters(vmBackup);
        parameters.setScratchDisksMap(getParameters().getScratchDisksMap());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);

        runInternalActionWithTasksContext(ActionType.RemoveScratchDisks, parameters);
    }

    private void removeCheckpointFromDb() {
        Guid vmCheckpointId = getParameters().getToCheckpointId();
        log.info("Remove VmCheckpoint entity '{}'", vmCheckpointId);

        TransactionSupport.executeInNewTransaction(() -> {
            vmCheckpointDao.remove(vmCheckpointId);
            return null;
        });
    }

    private Guid createVmBackup() {
        VmBackup vmBackup = getParameters().getVmBackup();
        Guid backupId = vmBackup.getId() != null ? vmBackup.getId() : getCommandId();
        vmBackup.setId(backupId);
        vmBackup.setHostId(getVdsId());
        vmBackup.setPhase(VmBackupPhase.INITIALIZING);
        Date now = new Date();
        vmBackup.setCreationDate(now);
        vmBackup.setModificationDate(now);
        vmBackup.setBackupType(isLiveBackup ? VmBackupType.Live : VmBackupType.Cold);

        getParameters().setVmBackup(vmBackup);
        TransactionSupport.executeInNewTransaction(() -> {
            vmBackupDao.save(vmBackup);
            getParameters().getVmBackup().getDisks().forEach(
                    disk -> {
                        setDiskBackupModeIfSupported(disk);
                        vmBackupDao.addDiskToVmBackup(vmBackup.getId(), disk.getId(), null);
                    });
            return null;
        });
        persistCommandIfNeeded();
        return vmBackup.getId();
    }

    protected void setDiskBackupModeIfSupported(DiskImage disk) {
        if (FeatureSupported.isBackupModeAndBitmapsOperationsSupported(getCluster().getCompatibilityVersion())) {
            DiskBackupMode diskBackupMode = getBackupModeForDisk(disk.getId(),
                    getParameters().getVmBackup().getFromCheckpointId());
            disk.setBackupMode(diskBackupMode);
            baseDiskDao.update(disk);
        }
    }

    private DiskBackupMode getBackupModeForDisk(Guid diskId, Guid fromCheckpointId) {
        if (fromCheckpointId == null) {
            return DiskBackupMode.Full;
        }
        if (!getFromCheckpointDisksIds(fromCheckpointId).contains(diskId)) {
            log.warn("Disk ID {} isn't included in checkpoint ID {}, a full backup will be performed for the disk.",
                    diskId, fromCheckpointId);
            return DiskBackupMode.Full;
        }
        return DiskBackupMode.Incremental;
    }

    protected Set<Guid> getFromCheckpointDisksIds(Guid fromCheckpointId) {
        if (fromCheckpointDisksIds == null) {
            fromCheckpointDisksIds = vmCheckpointDao.getDisksByCheckpointId(fromCheckpointId)
                    .stream()
                    .map(DiskImage::getId)
                    .collect(Collectors.toCollection(HashSet::new));
        }
        return fromCheckpointDisksIds;
    }

    protected Guid createVmCheckpoint(Guid checkpointId) {
        VmCheckpoint vmCheckpoint = new VmCheckpoint();

        vmCheckpoint.setId(checkpointId);
        VmCheckpoint checkpointsLeaf = getVmCheckpointsLeaf();
        if (checkpointsLeaf != null) {
            vmCheckpoint.setParentId(checkpointsLeaf.getId());
        }

        VmBackup vmBackup = getParameters().getVmBackup();

        vmCheckpoint.setVmId(vmBackup.getVmId());
        vmCheckpoint.setCreationDate(new Date());
        vmCheckpoint.setState(VmCheckpointState.CREATED);
        vmCheckpoint.setDescription(vmBackup.getDescription());

        TransactionSupport.executeInNewTransaction(() -> {
            vmCheckpointDao.save(vmCheckpoint);
            vmBackup.getDisks()
                    .stream()
                    .filter(DiskImage::isQcowFormat)
                    .forEach(disk -> vmCheckpointDao.addDiskToCheckpoint(vmCheckpoint.getId(), disk.getId()));
            return null;
        });

        persistCommandIfNeeded();
        return vmCheckpoint.getId();
    }

    private boolean isBackupContainsRawDisksOnly() {
        return getParameters().getVmBackup().getDisks()
                .stream()
                .noneMatch(DiskImage::isQcowFormat);
    }

    private VmBackupInfo performVmBackupOperation(VDSCommandType vdsCommandType,
            Map<Guid, ScratchDiskInfo> scratchDiskInfoMap) {
        // Add the created checkpoint ID.
        VmBackup vmBackup = getParameters().getVmBackup();
        vmBackup.setToCheckpointId(getParameters().getToCheckpointId());
        VmBackupVDSParameters parameters = new VmBackupVDSParameters(
                getVdsId(), vmBackup, getParameters().isRequireConsistency(), scratchDiskInfoMap);

        try {
            VDSReturnValue vdsRetVal = runVdsCommand(vdsCommandType, parameters);
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                throw engineException;
            }
            return (VmBackupInfo) vdsRetVal.getReturnValue();
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

    protected void restoreCommandState() {
        Guid backupId = getParameters().getVmBackup().getId();
        VmBackup vmBackup = vmBackupDao.get(backupId);
        vmBackup.setDisks(vmBackupDao.getDisksByBackupId(backupId));
        getParameters().setVmBackup(vmBackup);
    }

    protected void updateVmBackupPhase(VmBackupPhase phase) {
        VmBackup vmBackup = getParameters().getVmBackup();
        log.info("Change VM '{}' backup '{}' phase from '{}' to '{}'",
                getVmId(), vmBackup.getId(), vmBackup.getPhase(), phase);
        vmBackup.setPhase(phase);
        vmBackupDao.update(vmBackup);
    }

    private void updateVmBackupCheckpoint() {
        TransactionSupport.executeInNewTransaction(() -> {
            // Update the VmBackup to include the checkpoint ID.
            vmBackupDao.update(getParameters().getVmBackup());
            return null;
        });
    }

    private boolean createScratchDisks() {
        VmBackupParameters parameters = new VmBackupParameters(getParameters().getVmBackup());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.PARENT_MANAGED);

        ActionReturnValue returnValue = runInternalAction(ActionType.CreateScratchDisks, parameters);
        if (returnValue == null) {
            return false;
        }

        getParameters().setScratchDisksMap(returnValue.getActionReturnValue());
        return returnValue.getSucceeded();
    }

    private Map<Guid, ScratchDiskInfo> prepareScratchDisks() {
        Map<Guid, ScratchDiskInfo> scratchDiskInfoMap = new HashMap<>();
        for (Map.Entry<Guid, DiskImage> entry : getParameters().getScratchDisksMap().entrySet()) {
            Guid diskId = entry.getKey();
            DiskImage scratchDisk = entry.getValue();
            String scratchDiskPath = prepareImage(scratchDisk);

            if (scratchDiskPath == null) {
                log.error("Failed to prepare scratch disk '{}' for disk ID '{}'", scratchDisk.getId(), diskId);
                return null;
            }

            log.info("Scratch disk '{}' for disk ID '{}', path is: '{}'", scratchDisk.getId(), diskId, scratchDiskPath);
            scratchDiskInfoMap.put(diskId, new ScratchDiskInfo(scratchDisk, scratchDiskPath));
        }
        return scratchDiskInfoMap;
    }

    private String prepareImage(DiskImage diskImage) {
        log.info("Preparing image '{}/{}' on the VM host '{}'", diskImage.getId(), diskImage.getImageId(), getVdsId());
        try {
            VDSReturnValue vdsRetVal = imagesHandler.prepareImage(getStoragePoolId(),
                    diskImage.getStorageIds().get(0),
                    diskImage.getId(),
                    diskImage.getImageId(),
                    getVdsId());
            return ((PrepareImageReturn) vdsRetVal.getReturnValue()).getImagePath();
        } catch (EngineException e) {
            log.error("Failed to prepare image '{}/{}' on the VM host '{}'",
                    diskImage.getId(),
                    diskImage.getImageId(),
                    getVdsId());
            return null;
        }
    }

    private EngineLock getEntityUpdateLock(Guid backupId) {
        Map<String, Pair<String, String>> lockMap = Collections.singletonMap(
                backupId.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_BACKUP, EngineMessage.ACTION_TYPE_FAILED_VM_BACKUP_LOCKED));
        return new EngineLock(lockMap);
    }

    @Override
    protected void endSuccessfully() {
        finalizeVmBackup(VmBackupPhase.SUCCEEDED);
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        finalizeVmBackup(VmBackupPhase.FAILED);
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
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        getParameters().getVmBackup().getDisks().forEach(
                disk -> permissionList.add(
                        new PermissionSubject(disk.getId(), VdcObjectType.Disk, ActionGroup.BACKUP_DISK)));
        return permissionList;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        getDiskIds().forEach(id -> locks.put(id.toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_DISK_IS_LOCKED)));
        locks.put(getParameters().getVmBackup().getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_BACKUP));
        return locks;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        VmBackup vmBackup = getParameters().getVmBackup();
        addCustomValue("VmName", getVm().getName());
        addCustomValue("backupId", vmBackup.getId().toString());
        switch (getActionState()) {
            case EXECUTE:
                return AuditLogType.VM_BACKUP_STARTED;
            case END_FAILURE:
                return AuditLogType.VM_BACKUP_FAILED;
            case END_SUCCESS:
                if (!getSucceeded()) {
                    return AuditLogType.VM_BACKUP_FAILED;
                }
                if (vmBackup.getPhase() == VmBackupPhase.SUCCEEDED) {
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

    private void cleanDisksBackupModeIfSupported() {
        if (FeatureSupported.isBackupModeAndBitmapsOperationsSupported(getCluster().getCompatibilityVersion())) {
            TransactionSupport.executeInNewTransaction(() -> {
                getParameters().getVmBackup().getDisks().forEach(
                        disk -> {
                            disk.setBackupMode(null);
                            baseDiskDao.update(disk);
                        });
                return null;
            });
        }
    }

    private void unlockDisks() {
        imagesHandler.updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(
                getDiskIds(),
                ImageStatus.OK,
                ImageStatus.ILLEGAL,
                getCompensationContext());
    }

    public DiskExistenceValidator createDiskExistenceValidator(Set<Guid> disksGuids) {
        return Injector.injectMembers(new DiskExistenceValidator(disksGuids));
    }

    public DiskImagesValidator createDiskImagesValidator(List<DiskImage> disks) {
        return Injector.injectMembers(new DiskImagesValidator(disks));
    }

    public Set<Guid> getDiskIds() {
        List<DiskImage> disks = getParameters().getVmBackup().getDisks();
        return disks == null ? Collections.emptySet() : disks
                .stream()
                .map(DiskImage::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    protected List<DiskImage> getDisks() {
        if (disksList == null) {
            List<Disk> vmDisks = diskDao.getAllForVm(getVmId());
            List<DiskImage> diskImages = DisksFilter.filterImageDisks(vmDisks,
                    ONLY_NOT_SHAREABLE, ONLY_SNAPABLE, ONLY_ACTIVE);
            disksList = diskImages
                    .stream()
                    .filter(d -> getDiskIds().contains(d.getId()))
                    .collect(Collectors.toList());
        }
        return disksList;
    }

    public VmCheckpoint getVmCheckpointsLeaf() {
        if (vmCheckpointsLeaf == null) {
            List<VmCheckpoint> vmCheckpoints = vmCheckpointDao.getAllForVm(getVmId());
            if (!CollectionUtils.isEmpty(vmCheckpoints)) {
                vmCheckpointsLeaf = vmCheckpoints.get(vmCheckpoints.size() - 1);
            }
        }
        return vmCheckpointsLeaf;
    }

    private boolean isLiveBackup() {
        return getParameters().getVmBackup().getBackupType() == VmBackupType.Live;
    }
}
