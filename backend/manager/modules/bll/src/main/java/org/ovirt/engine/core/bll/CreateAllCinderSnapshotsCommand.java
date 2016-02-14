package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.CreateAllCinderSnapshotsParameters;
import org.ovirt.engine.core.common.action.CreateCinderSnapshotParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateAllCinderSnapshotsCommand<T extends CreateAllCinderSnapshotsParameters> extends CommandBase<T> {

    public CreateAllCinderSnapshotsCommand(Guid commandId) {
        super(commandId);
    }

    public CreateAllCinderSnapshotsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        List<CinderDisk> cinderDisks = getParameters().getCinderDisks();
        for (final CinderDisk cinderDisk : cinderDisks) {
            Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                    VdcActionType.CreateCinderSnapshot,
                    getCinderDiskSnapshotParameter(cinderDisk),
                    cloneContextAndDetachFromParent());
            try {
                VdcReturnValueBase vdcReturnValueBase = future.get();
                if (!vdcReturnValueBase.getSucceeded()) {
                    log.error("Error creating snapshot for Cinder disk '{}'", cinderDisk.getDiskAlias());
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error creating snapshot for Cinder disk '{}': {}", cinderDisk.getDiskAlias(), e.getMessage());
            }
        }
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    private ImagesContainterParametersBase getCinderDiskSnapshotParameter(CinderDisk cinderDisk) {
        CreateCinderSnapshotParameters createParams =
                new CreateCinderSnapshotParameters(((CinderDisk) getDiskDao().get(cinderDisk.getId())).getImageId());
        createParams.setVmSnapshotId(getParameters().getNewActiveSnapshotId());
        createParams.setStorageDomainId(cinderDisk.getStorageIds().get(0));
        createParams.setDescription(getParameters().getDescription());
        createParams.setSnapshotType(getParameters().getSnapshotType());
        return withRootCommandInfo(createParams, getActionType());
    }

    @Override
    public CommandCallback getCallback() {
        return new ConcurrentChildCommandsExecutionCallback();
    }
}
