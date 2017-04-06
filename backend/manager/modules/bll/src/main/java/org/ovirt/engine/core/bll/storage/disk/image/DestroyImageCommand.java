package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.PostDeleteActionHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.vdscommands.DestroyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
public class DestroyImageCommand<T extends DestroyImageParameters>
        extends CommandBase<T> {

    @Inject
    private PostDeleteActionHandler postDeleteActionHandler;

    @Inject
    private StorageDomainDao storageDomainDao;

    private static final Logger log = LoggerFactory.getLogger(DestroyImageCommand.class);

    public DestroyImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getImageGroupId()));

        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());

        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.DestroyImage,
                createVDSParameters());

        if (vdsReturnValue != null && vdsReturnValue.getCreationInfo() != null) {
            getParameters().setVdsmTaskIds(new ArrayList<>());
            Guid result = createTask(taskId, vdsReturnValue.getCreationInfo(), getParameters().getParentCommand(),
                            VdcObjectType.Storage, getParameters().getStorageDomainId());
            getReturnValue().getInternalVdsmTaskIdList().add(result);
            getReturnValue().getVdsmTaskIdList().add(result);
            getParameters().getVdsmTaskIds().add(result);
            setSucceeded(vdsReturnValue.getSucceeded());
            log.info("Successfully started task to remove orphaned volumes resulting from live merge");
        } else {
            setSucceeded(false);
            setCommandStatus(CommandStatus.FAILED);
        }
    }

    private VDSParametersBase createVDSParameters() {
        return postDeleteActionHandler.fixParameters(
                new DestroyImageVDSCommandParameters(
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getImageGroupId(),
                        getParameters().getImageList(),
                        getParameters().isPostZero(),
                        storageDomainDao.get(getParameters().getStorageDomainId()).isDiscardAfterDelete(),
                        getParameters().isForce()));
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
