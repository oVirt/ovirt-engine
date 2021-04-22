package org.ovirt.engine.core.bll.storage.lease;

import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExternalLeaseParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.AddExternalLeaseVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
public class RemoveExternalLeaseCommand <T extends ExternalLeaseParameters> extends CommandBase<T> {

    public RemoveExternalLeaseCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public RemoveExternalLeaseCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        AddExternalLeaseVDSParameters params = new AddExternalLeaseVDSParameters(getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getLeaseId(),
                null);
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.RemoveExternalLease, params);

        if (returnValue.getSucceeded()) {
            Guid vdsmTaskId = createTask(taskId,
                    returnValue.getCreationInfo(),
                    getParameters().getParentCommand(),
                    VdcObjectType.Storage,
                    getParameters().getStorageDomainId());
            getTaskIdList().add(vdsmTaskId);
            getReturnValue().getVdsmTaskIdList().add(vdsmTaskId);
        }

        setSucceeded(returnValue.getSucceeded());
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.removeLease;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }
}
