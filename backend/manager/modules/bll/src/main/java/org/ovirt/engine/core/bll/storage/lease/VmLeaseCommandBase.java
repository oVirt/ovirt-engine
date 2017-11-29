package org.ovirt.engine.core.bll.storage.lease;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmLeaseParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

abstract class VmLeaseCommandBase<T extends VmLeaseParameters> extends CommandBase<T> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public VmLeaseCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public VmLeaseCommandBase(T parameters) {
        this(parameters, null);
    }

    @Override
    protected boolean validate() {
        StorageDomain domain = storageDomainDao.getForStoragePool(getParameters().getStorageDomainId(),
                getParameters().getStoragePoolId());
        StorageDomainValidator validator = new StorageDomainValidator(domain);
        return validate(validator.isDomainExistAndActive()) && validate(validator.isDataDomain());
    }

    @Override
    protected void executeCommand() {
        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        VmLeaseVDSParameters params = new VmLeaseVDSParameters(getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(), getParameters().getVmId());
        VDSReturnValue returnValue = runVdsCommand(getLeaseAction(), params);
        if (returnValue.getSucceeded()) {
            getTaskIdList().add(
                    createTask(taskId,
                            returnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));
        }
        setSucceeded(returnValue.getSucceeded());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionCheckSubjects = new ArrayList<>();
        permissionCheckSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
        return permissionCheckSubjects;
    }
    protected abstract VDSCommandType getLeaseAction();

    protected abstract AsyncTaskType getTaskType();
}
