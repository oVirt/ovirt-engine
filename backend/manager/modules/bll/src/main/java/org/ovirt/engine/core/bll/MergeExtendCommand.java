package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExtendImageSizeParameters;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.action.RefreshVolumeParameters;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class MergeExtendCommand<T extends MergeParameters>
        extends CommandBase<T> {

    @Inject
    private ImageDao imageDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public MergeExtendCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void executeCommand() {
        if (getParameters().getBaseImage().hasRawBlock()) {
            if (getParameters().getTopImage().getSize() != getParameters().getBaseImage().getSize()) {
                // Only raw base volumes on block storage need explicit extension
                extendImageSize();
            } else if (getParameters().getBaseImage().getImageStatus() == ImageStatus.ILLEGAL) {
                // Refresh the image in case this is a Live Merge recovery from an execution that
                // extended the volume and updated the database but was unable to refresh the host.
                refreshImageOnHost();
            } else {
                log.info("Base and top image sizes are the same; no extension required");
                setCommandStatus(CommandStatus.SUCCEEDED);
            }
        } else {
            if (getParameters().getTopImage().getSize() != getParameters().getBaseImage().getSize()) {
                updateSizeInDb();
            } else {
                log.info("Base and top image sizes are the same; no image size update required");
            }
            setCommandStatus(CommandStatus.SUCCEEDED);
        }
        setSucceeded(true);
    }

    private void extendImageSize() {
        Guid diskImageId = getParameters().getBaseImage().getImageId();
        long sizeInBytes = getParameters().getTopImage().getSize();
        log.info("Extending size of base volume {} to {} bytes", diskImageId, sizeInBytes);

        ExtendImageSizeParameters parameters =
                new ExtendImageSizeParameters(diskImageId, sizeInBytes, true);
        parameters.setStoragePoolId(getParameters().getBaseImage().getStoragePoolId());
        parameters.setStorageDomainId(getParameters().getBaseImage().getStorageIds().get(0));
        parameters.setImageGroupID(getParameters().getBaseImage().getId());
        parameters.setParentCommand(ActionType.MergeExtend);
        parameters.setParentParameters(getParameters());

        commandCoordinatorUtil.executeAsyncCommand(
                ActionType.ExtendImageSize,
                parameters,
                cloneContextAndDetachFromParent());
    }

    private void refreshImageOnHost() {
        log.info("Refreshing volume {} on host {}",
                getParameters().getBaseImage().getImageId(), getParameters().getVdsId());

        RefreshVolumeParameters parameters = new RefreshVolumeParameters(
                getParameters().getVdsId(),
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(),
                getParameters().getBaseImage().getImageId());
        parameters.setParentCommand(ActionType.MergeExtend);
        parameters.setParentParameters(getParameters());

        ActionReturnValue returnValue = runInternalAction(ActionType.RefreshVolume, parameters);
        setSucceeded(returnValue.getSucceeded());
        if (!getSucceeded()) {
            log.error("Error refreshing volume {} on host {}, VMs using the volume"
                    + " should be restarted to detect the new size.");
        }
        setCommandStatus(getSucceeded() ? CommandStatus.SUCCEEDED : CommandStatus.FAILED);
    }

    private void updateSizeInDb() {
        Guid diskImage = getParameters().getBaseImage().getImageId();
        long sizeInBytes = getParameters().getTopImage().getSize();
        log.info("Updating size of image {} to {}", diskImage, sizeInBytes);

        imageDao.updateImageSize(diskImage, sizeInBytes);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
