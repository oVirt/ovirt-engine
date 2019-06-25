package org.ovirt.engine.core.bll.storage.lease;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmLeaseParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.dao.StorageDomainDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class GetVmLeaseInfoCommand<T extends VmLeaseParameters> extends CommandBase<T> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public GetVmLeaseInfoCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public GetVmLeaseInfoCommand(T parameters) {
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
        VDSReturnValue retVal;
        VmLeaseVDSParameters vmLeaseVDSParameters = new VmLeaseVDSParameters(
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getVmId());

        if (getParameters().isFailureExpected()) {
            vmLeaseVDSParameters.setExpectedEngineErrors(Collections.singleton(EngineError.NoSuchVmLeaseOnDomain));
        }

        try {
            retVal = runVdsCommand(VDSCommandType.GetVmLeaseInfo, vmLeaseVDSParameters);
        } catch (EngineException e) {
            if (!getParameters().isFailureExpected()) {
                log.error("Failure in getting lease info for VM '{}' from storage domains '{}', message: {}",
                        getParameters().getVmId(),
                        getParameters().getStorageDomainId(),
                        e.getMessage());
            }
            return;
        }

        if (retVal != null && retVal.getSucceeded()) {
            getReturnValue().setActionReturnValue(retVal.getReturnValue());
            setSucceeded(true);
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionCheckSubjects = new ArrayList<>();
        permissionCheckSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
        return permissionCheckSubjects;
    }
}
