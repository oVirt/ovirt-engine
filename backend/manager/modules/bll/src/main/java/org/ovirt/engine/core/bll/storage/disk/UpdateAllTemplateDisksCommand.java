package org.ovirt.engine.core.bll.storage.disk;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmTemplateCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UpdateAllTemplateDisksParameters;
import org.ovirt.engine.core.common.action.UpdateVolumeCommandParameters;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class UpdateAllTemplateDisksCommand<T extends UpdateAllTemplateDisksParameters> extends VmTemplateCommand<T> {

    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public UpdateAllTemplateDisksCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateAllTemplateDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private List<DiskImage> getDiskImages() {
        vmTemplateHandler.updateDisksFromDb(getVmTemplate());
        return DisksFilter.filterImageDisks(getVmTemplate().getDiskTemplateMap().values(),
                DisksFilter.ONLY_NOT_SHAREABLE);
    }

    @Override
    protected void executeCommand() {
        getDiskImages().forEach(this::updateDiskImage);
        setSucceeded(true);
    }

    private void updateDiskImage(DiskImage diskImage) {
        ActionReturnValue returnValue = runInternalActionWithTasksContext(
                ActionType.UpdateVolume,
                buildUpdateVolumeCommandParameters(diskImage));

        if (!returnValue.getSucceeded()) {
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }
    }

    private UpdateVolumeCommandParameters buildUpdateVolumeCommandParameters(DiskImage diskImage) {
        UpdateVolumeCommandParameters parameters = new UpdateVolumeCommandParameters(
                diskImage.getStoragePoolId(),
                new VdsmImageLocationInfo(diskImage),
                getParameters().getLegal(),
                null,
                null,
                getParameters().getShared());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return parameters;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

}
