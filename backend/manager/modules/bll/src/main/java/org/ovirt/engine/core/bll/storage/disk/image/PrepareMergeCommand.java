package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ColdMergeCommandParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.SPMColdMergeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class PrepareMergeCommand<T extends ColdMergeCommandParameters> extends CommandBase<T> {

    public PrepareMergeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        SPMColdMergeVDSCommandParameters parameters = new SPMColdMergeVDSCommandParameters(getParameters().getStoragePoolId(),
                getParameters().getSubchainInfo());

        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.PrepareMerge, parameters);
        if (vdsReturnValue.getSucceeded()) {
            Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
            getTaskIdList().add(createTask(taskId,
                    vdsReturnValue.getCreationInfo(),
                    getParameters().getParentCommand(),
                    VdcObjectType.Storage,
                    getParameters().getStorageDomainId()));
            setSucceeded(true);
        }
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.prepareMerge;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }
}
