package org.ovirt.engine.core.bll.snapshots;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.HostJobCommand;
import org.ovirt.engine.core.bll.VirtJobCallback;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.SnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLiveSnapshotForVmCommand<T extends CreateSnapshotForVmParameters> extends VmCommand<T>  implements HostJobCommand, QuotaStorageDependent {
    private static final Logger log = LoggerFactory.getLogger(CreateLiveSnapshotForVmCommand.class);
    private List<DiskImage> cachedSelectedActiveDisks;
    private List<DiskImage> cachedImagesDisks;
    private Snapshot snapshot;
    @Inject
    @Typed(VirtJobCallback.class)
    private Instance<VirtJobCallback> callbackProvider;
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private DiskDao diskDao;

    public CreateLiveSnapshotForVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        boolean liveSnapshotRunning = false;
        try {
            VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.Snapshot, buildLiveSnapshotParameters(getParameters().getSnapshot()));

            if (vdsReturnValue.getSucceeded()) {
                Guid jobId = (Guid) vdsReturnValue.getReturnValue();
                getParameters().setHostJobId(jobId);
                persistCommand(getParameters().getParentCommand(), true);
                log.debug("Live snapshot started successfully");
                liveSnapshotRunning = true;
            } else {
                log.error("Failed to start live snapshot on VDS");
            }
        } catch (EngineException e) {
            log.error("Engine exception thrown while sending live snapshot command", e);
            if (e.getErrorCode() == EngineError.imageErr || e.getErrorCode() == EngineError.SNAPSHOT_FAILED) {
                // In this case, we are not certain whether merge is currently running or
                // whether one of the relevant volumes already removed from the chain. In these cases,
                // we want to verify the current state; therefore, we consider the merge to be running.
                liveSnapshotRunning = true;
            }
        } finally {
            if (liveSnapshotRunning) {
                setSucceeded(true);
            } else {
                setCommandStatus(CommandStatus.FAILED);
                handleVdsLiveSnapshotFailure(new EngineException(EngineError.SNAPSHOT_FAILED));
            }
        }
    }

    private SnapshotVDSCommandParameters buildLiveSnapshotParameters(Snapshot snapshot) {
        List<Disk> pluggedDisksForVm = diskDao.getAllForVm(getVm().getId(), true);
        List<DiskImage> filteredPluggedDisksForVm = DisksFilter.filterImageDisks(pluggedDisksForVm,
                ONLY_SNAPABLE, ONLY_ACTIVE);

        // 'filteredPluggedDisks' should contain only disks from 'getDisksList()' that are plugged to the VM.
        List<DiskImage> filteredPluggedDisks = ImagesHandler.imagesIntersection(filteredPluggedDisksForVm, getDisksList());

        SnapshotVDSCommandParameters parameters = new SnapshotVDSCommandParameters(
                getVm().getRunOnVds(), getVm().getId(), filteredPluggedDisks);

        if (isMemorySnapshotSupported() && snapshot.containsMemory()) {
            parameters.setMemoryDump((DiskImage) diskDao.get(snapshot.getMemoryDiskId()));
            parameters.setMemoryConf((DiskImage) diskDao.get(snapshot.getMetadataDiskId()));
        }

        // In case the snapshot is auto-generated for live storage migration,
        // we do not want to issue an FS freeze thus setting vmFrozen to true
        // so a freeze will not be issued by Vdsm
        parameters.setVmFrozen(shouldFreezeOrThawVm() ||
                getParameters().getParentCommand() == ActionType.LiveMigrateDisk);

        return parameters;
    }

    private boolean shouldFreezeOrThawVm() {
        return isLiveSnapshotApplicable() &&
                (diskOfTypeExists(DiskStorageType.CINDER) || diskOfTypeExists(DiskStorageType.MANAGED_BLOCK_STORAGE)) &&
                getParameters().getParentCommand() != ActionType.LiveMigrateDisk;
    }

    private boolean diskOfTypeExists(DiskStorageType type) {
        return getDisksList()
                .stream()
                .anyMatch(disk -> type.equals(disk.getDiskStorageType()));
    }

    /**
     * Filter all allowed snapshot disks.
     *
     * @return list of disks to be snapshot.
     */
    protected List<DiskImage> getDisksList() {
        if (cachedSelectedActiveDisks == null) {
            List<DiskImage> imagesAndCinderForVm = getDiskImagesForVm();

            // Get disks from the specified parameters or according to the VM
            if (getParameters().getDiskIds() == null) {
                cachedSelectedActiveDisks = imagesAndCinderForVm;
            } else {
                // Get selected images from 'DiskImagesForVm' to ensure disks entities integrity
                // (i.e. only images' IDs and Cinders' IDs are relevant).
                cachedSelectedActiveDisks = getDiskImagesForVm().stream()
                        .filter(d -> getParameters().getDiskIds().contains(d.getId()))
                        .collect(Collectors.toList());
            }
        }
        return cachedSelectedActiveDisks;
    }

    protected List<DiskImage> getDiskImagesForVm() {
        List<Disk> disks = diskDao.getAllForVm(getVmId());
        List<DiskImage> allDisks = new ArrayList<>(getDiskImages(disks));
        allDisks.addAll(imagesHandler.getCinderLeafImages(disks));
        allDisks.addAll(imagesHandler.getManagedBlockStorageSnapshots(disks));
        return allDisks;
    }

    private List<DiskImage> getDiskImages(List<Disk> disks) {
        if (cachedImagesDisks == null) {
            cachedImagesDisks = DisksFilter.filterImageDisks(disks, ONLY_NOT_SHAREABLE,
                    ONLY_SNAPABLE, ONLY_ACTIVE);
        }

        return cachedImagesDisks;
    }

    protected boolean isLiveSnapshotApplicable() {
        return getParameters().getParentCommand() != ActionType.RunVm && getVm() != null
                && (getVm().isRunning() || getVm().getStatus() == VMStatus.Paused) && getVm().getRunOnVds() != null;
    }

    /**
     * Check if Memory Snapshot is supported
     */
    private boolean isMemorySnapshotSupported() {
        return FeatureSupported.isMemorySnapshotSupportedByArchitecture(
                getVm().getClusterArch(), getVm().getCompatibilityVersion());
    }

    private void handleVdsLiveSnapshotFailure(EngineException e) {
        setCommandStatus(CommandStatus.FAILED);
        handleVmFailure(e, AuditLogType.USER_CREATE_LIVE_SNAPSHOT_FINISHED_FAILURE,
                "Could not perform live snapshot due to error, VM will still be configured to the new created"
                        + " snapshot: {}");
    }

    private void handleVmFailure(EngineException e, AuditLogType auditLogType, String warnMessage) {
        log.warn(warnMessage, e.getMessage());
        log.debug("Exception", e);
        addCustomValue("SnapshotName", getSnapshotName());
        addCustomValue("VmName", getVmName());
        updateCallStackFromThrowable(e);
        auditLogDirector.log(this, auditLogType);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        for (DiskImage disk : getDisksList()) {
            list.add(new QuotaStorageConsumptionParameter(
                    disk.getQuotaId(),
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    disk.getStorageIds().get(0),
                    disk.getActualSize()));
        }

        return list;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public HostJobInfo.HostJobStatus handleJobError(EngineError error) {
        return HostJobInfo.HostJobStatus.failed;
    }

    @Override
    public boolean failJobWithUndeterminedStatus() {
        return false;
    }
}
