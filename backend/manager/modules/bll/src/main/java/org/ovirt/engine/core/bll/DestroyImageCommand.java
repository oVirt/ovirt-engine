package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.DestroyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

@InternalCommandAttribute
public class DestroyImageCommand<T extends DestroyImageParameters>
        extends CommandBase<T> {
    private static final Log log = LogFactory.getLog(DestroyImageCommand.class);

    public DestroyImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        setCommandStatus(CommandStatus.ACTIVE_ASYNC);
        Guid taskId = persistAsyncTaskPlaceHolder(VdcActionType.DestroyImage);

        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.DestroyImage,
                createVDSParameters());

        if (vdsReturnValue != null && vdsReturnValue.getCreationInfo() != null) {
            getParameters().setVdsmTaskIds(new ArrayList<Guid>());
            Guid result = createTask(taskId, vdsReturnValue.getCreationInfo(), VdcActionType.DestroyImage,
                            VdcObjectType.Storage, getParameters().getStorageDomainId());
            getReturnValue().getInternalVdsmTaskIdList().add(result);
            getReturnValue().getVdsmTaskIdList().add(result);
            getParameters().getVdsmTaskIds().add(result);
            setSucceeded(vdsReturnValue.getSucceeded());
            persistCommand(getParameters().getParentCommand(), true);
            log.info("Successfully started task to remove orphaned volumes resulting from live merge");
        } else {
            setSucceeded(false);
            setCommandStatus(CommandStatus.FAILED);
        }
    }

    private VDSParametersBase createVDSParameters() {
        return new DestroyImageVDSCommandParameters(
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(),
                getParameters().getImageList(),
                getParameters().isPostZero(),
                getParameters().isForce());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }

    @Override
    public AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteVolume;
    }
}
