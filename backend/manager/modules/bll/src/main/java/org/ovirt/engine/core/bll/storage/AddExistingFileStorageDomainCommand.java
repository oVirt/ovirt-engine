package org.ovirt.engine.core.bll.storage;

import org.apache.commons.lang.StringUtils;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class AddExistingFileStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        AddNFSStorageDomainCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddExistingFileStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddExistingFileStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canAddDomain() {
        return checkExistingStorageDomain();
    }

    @Override
    protected void executeCommand() {
        if (StringUtils.isEmpty(getStorageDomain().getStorage())) {
            getStorageDomain().setStorage(
                    (String) Backend
                            .getInstance()
                            .runInternalAction(
                                    VdcActionType.AddStorageServerConnection,
                                    new StorageServerConnectionParametersBase(getStorageDomain().getStorageStaticData()
                                            .getConnection(), getVds().getId())).getActionReturnValue());
        }
        addStorageDomainInDb();
        updateStorageDomainDynamicFromIrs();
        setSucceeded(true);
    }

    @Override
    protected boolean concreteCheckExistingStorageDomain(Pair<StorageDomainStatic, SANState> domain) {
        boolean returnValue = false;
        StorageDomainStatic domainFromIrs = domain.getFirst();
        if (StringUtils.isEmpty(getStorageDomain().getStorageStaticData().getStorage())
                && StringUtils.isEmpty(domainFromIrs.getStorage()) && domainFromIrs.getConnection() != null
                && getStorageDomain().getStorageStaticData().getConnection() != null) {
            returnValue = (StringUtils.equals(domainFromIrs.getConnection().getconnection(), getStorageDomain()
                    .getStorageStaticData().getConnection().getconnection()));
        } else if (!StringUtils.isEmpty(getStorageDomain().getStorageStaticData().getStorage())
                && !StringUtils.isEmpty(domainFromIrs.getStorage())) {
            returnValue = (StringUtils.equals(domainFromIrs.getStorage(), getStorageDomain().getStorageStaticData()
                    .getStorage()));
        }
        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_EXISTING_STORAGE_DOMAIN_CONNECTION_DATA_ILLEGAL);
        }
        return returnValue;
    }
}
