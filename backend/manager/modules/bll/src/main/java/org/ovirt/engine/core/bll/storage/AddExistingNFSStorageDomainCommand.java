package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.Pair;

public class AddExistingNFSStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        AddNFSStorageDomainCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddExistingNFSStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddExistingNFSStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean CanAddDomain() {
        return CheckExistingStorageDomain();
    }

    @Override
    protected void executeCommand() {
        if (StringHelper.isNullOrEmpty(getStorageDomain().getstorage())) {
            getStorageDomain().setstorage(
                    (String) Backend
                            .getInstance()
                            .runInternalAction(
                                    VdcActionType.AddStorageServerConnection,
                                    new StorageServerConnectionParametersBase(getStorageDomain().getStorageStaticData()
                                            .getConnection(), getVds().getId())).getActionReturnValue());
        }
        AddStorageDomainInDb();
        UpdateStorageDomainDynamicFromIrs();
        setSucceeded(true);
    }

    @Override
    protected boolean ConcreteCheckExistingStorageDomain(Pair<storage_domain_static, SANState> domain) {
        boolean returnValue = false;
        storage_domain_static domainFromIrs = domain.getFirst();
        if (StringHelper.isNullOrEmpty(getStorageDomain().getStorageStaticData().getstorage())
                && StringHelper.isNullOrEmpty(domainFromIrs.getstorage()) && domainFromIrs.getConnection() != null
                && getStorageDomain().getStorageStaticData().getConnection() != null) {
            returnValue = (StringHelper.EqOp(domainFromIrs.getConnection().getconnection(), getStorageDomain()
                    .getStorageStaticData().getConnection().getconnection()));
        } else if (!StringHelper.isNullOrEmpty(getStorageDomain().getStorageStaticData().getstorage())
                && !StringHelper.isNullOrEmpty(domainFromIrs.getstorage())) {
            returnValue = (StringHelper.EqOp(domainFromIrs.getstorage(), getStorageDomain().getStorageStaticData()
                    .getstorage()));
        }
        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_EXISTING_STORAGE_DOMAIN_CONNECTION_DATA_ILLEGAL);
        }
        return returnValue;
    }
}
