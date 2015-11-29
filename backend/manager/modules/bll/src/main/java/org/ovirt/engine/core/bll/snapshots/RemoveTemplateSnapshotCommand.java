package org.ovirt.engine.core.bll.snapshots;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.domain.PostZeroHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

/**
 * This command is responsible for removing a template image.
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RemoveTemplateSnapshotCommand<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {
    public RemoveTemplateSnapshotCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        Guid taskId = persistAsyncTaskPlaceHolder(VdcActionType.RemoveVmTemplate);

        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.DeleteImageGroup,
                PostZeroHandler.fixParametersWithPostZero(
                        new DeleteImageGroupVDSCommandParameters(getParameters().getStoragePoolId(),
                                getParameters().getStorageDomainId(), getParameters().getImageGroupID(),
                                getParameters().getWipeAfterDelete(), false)));

        if (vdsReturnValue.getSucceeded()) {
            getReturnValue().getInternalVdsmTaskIdList().add(
                    createTask(taskId,
                            vdsReturnValue.getCreationInfo(),
                            VdcActionType.RemoveVmTemplate,
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));

            setSucceeded(true);
        }
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteImage;
    }
}
