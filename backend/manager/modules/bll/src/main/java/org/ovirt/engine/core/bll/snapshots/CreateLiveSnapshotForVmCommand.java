package org.ovirt.engine.core.bll.snapshots;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.HostJobCommand;
import org.ovirt.engine.core.bll.VirtJobCallback;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.SnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class CreateLiveSnapshotForVmCommand<T extends CreateSnapshotForVmParameters> extends VmCommand<T> implements HostJobCommand {

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
        if (getParameters().isLegacyFlow()) {
            performLiveSnapshotDeprecated();
            thawVm();
        } else {
            try {
                VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.Snapshot, buildLiveSnapshotParameters(getParameters().getSnapshot()));

                if (vdsReturnValue.getSucceeded()) {
                    Guid jobId = (Guid) vdsReturnValue.getReturnValue();
                    getParameters().setHostJobId(jobId);
                    persistCommand(getParameters().getParentCommand(), true);
                    log.debug("Live snapshot started successfully");
                    setSucceeded(true);
                } else {
                    log.error("Failed to start live snapshot on VDS");
                    setCommandStatus(CommandStatus.FAILED);
                    setSucceeded(false);
                }
            } catch (EngineException e) {
                log.error("Engine exception thrown while sending live snapshot command", e);
                setCommandStatus(CommandStatus.FAILED);
                handleVdsLiveSnapshotFailure(e);
            }
        }
    }

    private SnapshotVDSCommandParameters buildLiveSnapshotParameters(Snapshot snapshot) {
        List<Disk> pluggedDisksForVm = diskDao.getAllForVm(getVm().getId(), true);
        List<DiskImage> filteredPluggedDisksForVm = DisksFilter.filterImageDisks(pluggedDisksForVm,
                ONLY_SNAPABLE, ONLY_ACTIVE);
        boolean memoryDump = getParameters().isMemorySnapshotSupported() && snapshot.containsMemory();

        // 'filteredPluggedDisks' should contain only disks from 'getDisksList()' that are plugged to the VM.
        List<DiskImage> filteredPluggedDisks = ImagesHandler.imagesIntersection(filteredPluggedDisksForVm, getParameters().getCachedSelectedActiveDisks());

        SnapshotVDSCommandParameters parameters = new SnapshotVDSCommandParameters(
                getVm().getRunOnVds(), getVm().getId(), filteredPluggedDisks);

        if (memoryDump) {
            parameters.setMemoryDump((DiskImage) diskDao.get(snapshot.getMemoryDiskId()));
            parameters.setMemoryConf((DiskImage) diskDao.get(snapshot.getMetadataDiskId()));
        }

        // In case the snapshot is auto-generated for live storage migration,
        // we do not want to issue an FS freeze thus setting vmFrozen to true
        // so a freeze will not be issued by Vdsm
        parameters.setVmFrozen(getParameters().getShouldFreezeOrThaw() || getParameters().isParentLiveMigrateDisk());

        if (!getParameters().isLegacyFlow()) {
            // Get the Live Snapshot timeout
            if (memoryDump) {
                parameters.setLiveSnapshotTimeout(Config.getValue(ConfigValues.LiveSnapshotTimeoutInMinutes));
            } else {
                parameters.setLiveSnapshotTimeout(Config.getValue(ConfigValues.LiveSnapshotFreezeTimeout));
            }
        }

        return parameters;
    }

    @Deprecated
    private void performLiveSnapshotDeprecated() {
        // Compatibility for  < 4.4 clusters.
        try {
            TransactionSupport.executeInScope(TransactionScopeOption.Suppress, () -> {
                runVdsCommand(VDSCommandType.Snapshot, buildLiveSnapshotParameters(getParameters().getSnapshot()));
                return null;
            });
        } catch (EngineException e) {
            handleVdsLiveSnapshotFailure(e);
            setSucceeded(false);
            setCommandStatus(CommandStatus.FAILED);
            return;
        }
        setSucceeded(true);
    }

    /**
     * VM thaw is needed if the VM was frozen.
     */
    private void thawVm() {
        if (!getParameters().getShouldFreezeOrThaw()) {
            return;
        }

        VDSReturnValue returnValue = null;
        boolean allowInconsistent = Config.<Boolean>getValue(ConfigValues.LiveSnapshotAllowInconsistent);
        try {
            returnValue = runVdsCommand(VDSCommandType.Thaw, new VdsAndVmIDVDSParametersBase(
                    getVds().getId(), getVmId()));
        } catch (EngineException e) {
            if (!allowInconsistent) {
                handleThawVmFailure(e);
                return;
            }
        }
        if (returnValue != null && !returnValue.getSucceeded() && !allowInconsistent) {
            handleThawVmFailure(new EngineException(EngineError.thawErr));
        }
    }

    private void handleThawVmFailure(EngineException e) {
        handleVmFailure(e, AuditLogType.FAILED_TO_THAW_VM,
                "Could not thaw VM guest filesystems due to an error: {}");
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
        addCustomValue("SnapshotName", getParameters().getSnapshot().getDescription());
        addCustomValue("VmName", getVmName());
        String translatedError = backend.getVdsErrorsTranslator().translateErrorTextSingle(e.getVdsError().getCode().toString());
        addCustomValue("DueToError", translatedError.isEmpty() ? "" : " due to: " + translatedError);
        updateCallStackFromThrowable(e);
        auditLogDirector.log(this, auditLogType);
    }

    @Override
    public ActionReturnValue endAction() {
        thawVm();
        return super.endAction();
    }

    @Override
    public CommandCallback getCallback() {
        if (getParameters().isLegacyFlow()) {
            return null;
        }
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
