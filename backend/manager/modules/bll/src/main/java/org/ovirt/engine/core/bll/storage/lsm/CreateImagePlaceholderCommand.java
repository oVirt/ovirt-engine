package org.ovirt.engine.core.bll.storage.lsm;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateImagePlaceholderCommandParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.vdscommands.TargetDomainImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CreateImagePlaceholderCommand<T extends CreateImagePlaceholderCommandParameters> extends CommandBase<T> {

    @Inject
    private DiskDao diskDao;

    public CreateImagePlaceholderCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getStoragePoolId());
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected void executeCommand() {
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getImageGroup()));
        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.CloneImageGroupStructure,
                new TargetDomainImageGroupVDSCommandParameters(
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getImageGroup(),
                        getParameters().getDstStorageDomain()));

        if (vdsReturnValue.getSucceeded()) {
            getTaskIdList().add(
                    createTask(taskId,
                            vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));
        }

        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public AsyncTaskType getTaskType() {
        return AsyncTaskType.cloneImageStructure;
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }

    private DiskImage getDiskImage() {
        return (DiskImage) diskDao.get(getParameters().getImageGroup());
    }
}
