package org.ovirt.engine.core.bll.storage;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
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
        updateStorageDomainWipeAfterDelete();
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

    protected boolean checkExistingStorageDomain() {
        if (getStorageDomainStaticDAO().get(getStorageDomain().getId()) != null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
        }

        Pair<StorageDomainStatic, Guid> domainFromIrs = executeHSMGetStorageDomainInfo(
                new HSMGetStorageDomainInfoVDSCommandParameters(getVdsId(), getStorageDomain().getId()));

        if (domainFromIrs == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }

        StorageDomain storageDomainFromIrs = new StorageDomain();
        storageDomainFromIrs.setStorageStaticData(domainFromIrs.getFirst());
        StorageDomainValidator validator = new StorageDomainValidator(storageDomainFromIrs);
        if (!validate(validator.isHostedEngineStorage())) {
            return false;
        }

        return concreteCheckExistingStorageDomain(domainFromIrs);
    }

    protected Pair<StorageDomainStatic, Guid> executeHSMGetStorageDomainInfo(HSMGetStorageDomainInfoVDSCommandParameters parameters) {
        return (Pair<StorageDomainStatic, Guid>) runVdsCommand(VDSCommandType.HSMGetStorageDomainInfo, parameters).getReturnValue();
    }

    protected boolean concreteCheckExistingStorageDomain(Pair<StorageDomainStatic, Guid> domain) {
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
