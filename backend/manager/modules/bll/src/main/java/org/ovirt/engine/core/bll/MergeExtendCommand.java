package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallBack;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExtendImageSizeParameters;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class MergeExtendCommand<T extends MergeParameters>
        extends CommandBase<T> {
    private static final Logger log = LoggerFactory.getLogger(MergeCommand.class);

    public MergeExtendCommand(T parameters) {
        super(parameters);
    }

    public MergeExtendCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public void executeCommand() {
        if (getParameters().getTopImage().getSize() == getParameters().getBaseImage().getSize()) {
            log.debug("No image size update required");
            setSucceeded(true);
            setCommandStatus(CommandStatus.SUCCEEDED);
            return;
        }

        // Only raw base volumes on block storage need explicit extension; others
        // only need their size updated in the database.
        if (isBaseRawBlock()) {
            extendImageSize();
        } else {
            updateSizeInDb();
            setCommandStatus(CommandStatus.SUCCEEDED);
        }
        setSucceeded(true);
    }

    private boolean isBaseRawBlock() {
        if (getParameters().getBaseImage().getVolumeFormat() == VolumeFormat.RAW) {
            List<image_storage_domain_map> maps = getDbFacade().getImageStorageDomainMapDao()
                    .getAllByImageId(getParameters().getBaseImage().getImageId());
            if (!maps.isEmpty()
                    && getStorageDomainDAO().get(maps.get(0).getstorage_domain_id())
                    .getStorageType().isBlockDomain()) {
                return true;
            }
        }
        return false;
    }

    private void extendImageSize() {
        Guid diskImageId = getParameters().getBaseImage().getImageId();
        long sizeInBytes = getParameters().getTopImage().getSize();
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
        log.info("Extending size of base volume {} to {} bytes", diskImageId, sizeInBytes);
    }

    private void updateSizeInDb() {
        Guid diskImage = getParameters().getBaseImage().getImageId();
        long sizeInBytes = getParameters().getTopImage().getSize();

        getDbFacade().getImageDao().updateImageSize(diskImage, sizeInBytes);
        log.info("Updated size of image {} to {}", diskImage, sizeInBytes);
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
            if (command != null && cmdEntity != null && !cmdEntity.isCallBackNotified()) {
                if (!succeeded) {
                    command.getParameters().setTaskGroupSuccess(false);
                }
                Backend.getInstance().endAction(VdcActionType.ExtendImageSize,
                        command.getParameters(),
                        cloneContextAndDetachFromParent());
                if (succeeded) {
                    CommandCoordinatorUtil.getCommandEntity(currentChildId).setCallBackNotified(true);
                }
            }
        }
    }

    @Override
    public CommandCallBack getCallBack() {
        return new MergeExtendCommandCallback();
    }
}
