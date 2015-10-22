package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExtendImageSizeParameters;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.action.RefreshVolumeParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class MergeExtendCommand<T extends MergeParameters>
        extends CommandBase<T> {
    public MergeExtendCommand(T parameters) {
        super(parameters);
    }

    public MergeExtendCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public void executeCommand() {
        if (ImagesHandler.isDiskImageRawBlock(getParameters().getBaseImage())) {
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
        parameters.setParentCommand(VdcActionType.MergeExtend);
        parameters.setParentParameters(getParameters());

        CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.ExtendImageSize,
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
        parameters.setParentCommand(VdcActionType.MergeExtend);
        parameters.setParentParameters(getParameters());

        VdcReturnValueBase returnValue = runInternalAction(VdcActionType.RefreshVolume, parameters);
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

        getDbFacade().getImageDao().updateImageSize(diskImage, sizeInBytes);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }

    @Override
    protected void endWithFailure() {
        handleAnyChildSPMTaskCompletion(false);
    }

    @Override
    protected void endSuccessfully() {
        handleAnyChildSPMTaskCompletion(true);
        setSucceeded(true);
    }

    private void handleAnyChildSPMTaskCompletion(boolean succeeded) {
        List<Guid> childCommandIds = CommandCoordinatorUtil.getChildCommandIds(getCommandId());
        if (childCommandIds.isEmpty()) {
            return;
        }
        Guid currentChildId = childCommandIds.get(0);
        log.info("Handling child command {} completion", currentChildId);

        if (!Guid.isNullOrEmpty(currentChildId)) {
            CommandBase<?> command = CommandCoordinatorUtil.retrieveCommand(currentChildId);
            CommandEntity cmdEntity = CommandCoordinatorUtil.getCommandEntity(currentChildId);
            if (command != null && cmdEntity != null && !cmdEntity.isCallbackNotified()) {
                if (!succeeded) {
                    command.getParameters().setTaskGroupSuccess(false);
                }
                Backend.getInstance().endAction(VdcActionType.ExtendImageSize,
                        command.getParameters(),
                        cloneContextAndDetachFromParent());
                if (succeeded) {
                    cmdEntity.setCallbackNotified(true);
                }
            }
        }
    }

    @Override
    public CommandCallback getCallback() {
        return new MergeExtendCommandCallback();
    }
}
