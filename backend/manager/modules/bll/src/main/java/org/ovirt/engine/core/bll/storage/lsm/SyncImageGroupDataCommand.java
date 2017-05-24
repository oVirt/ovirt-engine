package org.ovirt.engine.core.bll.storage.lsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.SyncImageGroupDataCommandParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.vdscommands.SyncImageGroupDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class SyncImageGroupDataCommand<T extends SyncImageGroupDataCommandParameters> extends CommandBase<T> {
    public SyncImageGroupDataCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getImageGroup()));
        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.SyncImageGroupData,
                new SyncImageGroupDataVDSCommandParameters(
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getImageGroup(),
                        getParameters().getDstStorageDomain(),
                        SyncImageGroupDataVDSCommandParameters.SYNC_TYPE_INTERNAL));

        if (vdsReturnValue.getSucceeded()) {
            getParameters().setVdsmTaskIds(new ArrayList<>());
            getParameters().getVdsmTaskIds().add(
                    createTask(taskId,
                            vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));
            getReturnValue().getVdsmTaskIdList().add(getParameters().getVdsmTaskIds().get(0));
        }

        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public AsyncTaskType getTaskType() {
        return AsyncTaskType.syncImageData;
    }
}
