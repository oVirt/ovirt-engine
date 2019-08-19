package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.action.MergeStatusReturnValue;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskStep;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.ReconcileVolumeChainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class MergeStatusCommand<T extends MergeParameters>
        extends CommandBase<T> {
    private static final Logger log = LoggerFactory.getLogger(MergeStatusCommand.class);

    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private ImagesHandler imagesHandler;

    public MergeStatusCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        attemptResolution();
    }

    public void attemptResolution() {
        Set<Guid> images;
        if (vmDynamicDao.get(getParameters().getVmId()).getStatus().isNotRunning()) {
            StoragePool pool = storagePoolDao.get(getParameters().getStoragePoolId());
            if (pool.getSpmVdsId() == null || pool.getStatus() != StoragePoolStatus.Up) {
                log.info("VM down, waiting on SPM election to resolve Live Merge");
                setSucceeded(true);
                return;
            } else {
                log.error("VM is not running, proceeding with Live Merge recovery");
                images = getVolumeChainFromRecovery();
            }
        } else {
            images = imagesHandler.getVolumeChain(getParameters().getVmId(),
                                                  getParameters().getVdsId(),
                                                  getParameters().getActiveImage());
        }
        if (images == null || images.isEmpty()) {
            log.error("Failed to retrieve images list of VM {}. Retrying ...", getParameters().getVmId());
            setCommandStatus(CommandStatus.SUCCEEDED);
            setSucceeded(true);
            // As this command is executed only during live merge flow, the following casting is safe.
            ((RemoveSnapshotSingleDiskParameters) getParameters().getParentParameters()).
                    setNextCommandStep(RemoveSnapshotSingleDiskStep.MERGE_STATUS);
            return;
        }

        Guid topImage = getParameters().getTopImage().getImageId();
        if (images.contains(topImage)) {
            // If the top volume is found in qemu chain, it means that the top volume wasn't deleted,
            // thus we have to fail live merge, reporting that the top volume is still in the chain.
            log.error("Failed to live merge. Top volume {} is still in qemu chain {}", topImage, images);
            setCommandStatus(CommandStatus.FAILED);
            return;
        }

        if (!images.contains(getParameters().getBaseImage().getImageId())) {
            // If the base image isn't found in qemu chain, it means that the image was already deleted.
            // In this case, we will ask the user to check if the parent snapshot contains illegal volume(s).
            // If so, that snapshot must be deleted before deleting other snapshots
            addCustomValue("SnapshotName", snapshotDao.get(getParameters().getBaseImage().getSnapshotId()).getDescription());
            addCustomValue("BaseVolumeId", getParameters().getBaseImage().getImageId().toString());
            auditLog(this, AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE_BASE_IMAGE_NOT_FOUND);
            setCommandStatus(CommandStatus.FAILED);
            return;
        }

        log.info("Successfully removed volume {} from the chain", topImage);

        // For now, only COMMIT type is supported
        log.info("Volume merge type '{}'", VmBlockJobType.COMMIT.name());

        MergeStatusReturnValue returnValue = new MergeStatusReturnValue(Collections.singleton(topImage));
        getReturnValue().setActionReturnValue(returnValue);
        setSucceeded(true);
        persistCommand(getParameters().getParentCommand(), true);
        setCommandStatus(CommandStatus.SUCCEEDED);
    }

    private Set<Guid> getVolumeChainFromRecovery() {
        ReconcileVolumeChainVDSCommandParameters parameters =
                new ReconcileVolumeChainVDSCommandParameters(
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getImageGroupId(),
                        getParameters().getImageId()
                );

        try {
            VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.ReconcileVolumeChain,
                    parameters);
            if (!vdsReturnValue.getSucceeded()) {
                log.error("Unable to retrieve volume list during Live Merge recovery");
                return null;
            }
            return new HashSet<>((List<Guid>) vdsReturnValue.getReturnValue());
        } catch (EngineException e) {
            log.error("Unable to retrieve volume list during Live Merge recovery", e);
            return null;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }
}
