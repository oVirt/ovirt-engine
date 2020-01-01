package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.managedblock.util.ManagedBlockStorageDiskUtil;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.RestoreAllManagedBlockSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RestoreAllManagedBlockSnapshotsCommand<T extends RestoreAllManagedBlockSnapshotsParameters>
        extends CommandBase<T> {

    @Inject
    private ManagedBlockStorageDiskUtil managedBlockStorageDiskUtil;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public RestoreAllManagedBlockSnapshotsCommand(T parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public RestoreAllManagedBlockSnapshotsCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        boolean succeeded = getParameters().getSnapshotAction() == SnapshotActionEnum.UNDO ?
                handleUndo() :
                handleCommit();
        setSucceeded(succeeded);

        persistCommandIfNeeded();
    }

    private boolean handleUndo() {
        // When undoing a preview snapshot we want to remove
        // the clone of the volume we created for the snapshot
        List<Guid> failedToRemoveDisks = new ArrayList<>();

        getParameters().getManagedBlockStorageDisks()
                .forEach(disk -> {
                    TransactionSupport.executeInNewTransaction(() -> {
                        managedBlockStorageDiskUtil.updateOldImageAsActive(Snapshot.SnapshotType.PREVIEW,
                                true,
                                disk);
                        return null;
                    });

                    // Remove cloned volume
                    if (!removeDisk(disk)) {
                        failedToRemoveDisks.add(disk.getImageId());
                    }
                });

        if (!failedToRemoveDisks.isEmpty()) {
            addCustomValue("DiskGuids", StringUtils.join(failedToRemoveDisks, ", "));
            auditLogDirector.log(this, AuditLogType.UNDO_SNAPSHOT_FAILURE_PARTIAL);
            return false;
        }

        return true;
    }

    private boolean handleCommit() {
        // Snapshots to remove from the cinderlib DB
        List<DiskImage> imagesToRemove = new ArrayList<>();

        // Snapshots to remove from engine DB
        List<Guid> snapshotsToRemove = new ArrayList<>();

        getParameters().getManagedBlockStorageDisks().forEach(disk -> {
            // Keep the cloned image we want to commit
            Guid imageTokeep = disk.getImageId();

            // TODO: replace with a dedicated SP
            List<DiskImage> allSnapshotsForParent = diskImageDao.getAllSnapshotsForParent(disk.getParentId());
            allSnapshotsForParent.stream()
                    .filter(diskImage -> !diskImage.getImageId().equals(imageTokeep))
                    .forEach(image -> {
                        imagesToRemove.add(image);
                        snapshotsToRemove.add(image.getSnapshotId());
                    });
        });

        getParameters().setSnapshotsToRemove(snapshotsToRemove);

        return imagesToRemove
                .stream()
                .allMatch(disk -> removeSnapshot((ManagedBlockStorageDisk) disk));
    }

    private boolean removeDisk(ManagedBlockStorageDisk disk) {
        RemoveDiskParameters params = new RemoveDiskParameters();
        params.setStorageDomainId(disk.getStorageIds().get(0));
        params.setDiskId(disk.getImageId());
        Future<ActionReturnValue> actionReturnValueFuture =
                commandCoordinatorUtil.executeAsyncCommand(ActionType.RemoveManagedBlockStorageDisk,
                        params,
                        cloneContextAndDetachFromParent());

        boolean succeeded = false;
        try {
            succeeded = actionReturnValueFuture.get().getSucceeded();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Remove cloned volume failed: ", e);
        }

        return succeeded;
    }

    private boolean removeSnapshot(ManagedBlockStorageDisk disk) {
        ImagesContainterParametersBase params = new ImagesContainterParametersBase();
        params.setImageGroupID(disk.getId());
        params.setImageId(disk.getImageId());
        params.setStorageDomainId(disk.getStorageIds().get(0));
        Future<ActionReturnValue> actionReturnValue =
                commandCoordinatorUtil.executeAsyncCommand(ActionType.RemoveManagedBlockStorageSnapshot,
                        params,
                        cloneContextAndDetachFromParent());
        boolean succeeded = false;
        try {
            succeeded = actionReturnValue.get().getSucceeded();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Removal of snapshot failed due to: ", e);
        }

        return succeeded;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void endSuccessfully() {
        TransactionSupport.executeInNewTransaction(() -> {
            getParameters().getSnapshotsToRemove().forEach(snapshotDao::remove);
            return null;
        });

        setSucceeded(true);
    }
}
