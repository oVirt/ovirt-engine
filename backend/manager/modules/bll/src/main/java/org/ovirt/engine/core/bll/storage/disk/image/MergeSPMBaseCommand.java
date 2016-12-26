package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ColdMergeCommandParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.vdscommands.SPMColdMergeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

public abstract class MergeSPMBaseCommand<T extends ColdMergeCommandParameters> extends CommandBase<T> {

    public MergeSPMBaseCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected void executeSPMMergeCommand(VDSCommandType vdsCommandType) {
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getSubchainInfo().getImageGroupId()));
        SPMColdMergeVDSCommandParameters parameters = new SPMColdMergeVDSCommandParameters(getParameters().getStoragePoolId(),
                getParameters().getSubchainInfo());

        VDSReturnValue vdsReturnValue = runVdsCommand(vdsCommandType, parameters);
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
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }
}
