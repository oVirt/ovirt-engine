package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;

public class AddExistingFileStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        AddNFSStorageDomainCommand<T> {

    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public AddExistingFileStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddExistingFileStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean canAddDomain() {
        return checkExistingStorageDomain();
    }

    @Override
    protected void executeCommand() {
        updateStaticDataDefaults();
        if (StringUtils.isEmpty(getStorageDomain().getStorage())) {
            getStorageDomain().setStorage(
                    backend
                    .runInternalAction(
                            ActionType.AddStorageServerConnection,
                            new StorageServerConnectionParametersBase(getStorageDomain().getStorageStaticData()
                                    .getConnection(), getVds().getId(), false)).getActionReturnValue());
        }
        addStorageDomainInDb();
        updateStorageDomainFromIrs();
        setSucceeded(true);
    }

    protected boolean checkExistingStorageDomain() {
        if (storageDomainStaticDao.get(getStorageDomain().getId()) != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
        }

        Pair<StorageDomainStatic, Guid> domainFromIrs = executeHSMGetStorageDomainInfo(
                new HSMGetStorageDomainInfoVDSCommandParameters(getVdsId(), getStorageDomain().getId()));

        if (domainFromIrs == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }

        if (!concreteCheckExistingStorageDomain(domainFromIrs)) {
            return false;
        }
        initStorageDomainProperties(domainFromIrs);
        return true;
    }

    private void initStorageDomainProperties(Pair<StorageDomainStatic, Guid> domain) {
        StorageDomainStatic domainFromIrs = domain.getFirst();
        if (StringUtils.isEmpty(getStorageDomain().getStorageStaticData().getName())) {
            getStorageDomain().getStorageStaticData().setStorageName(domainFromIrs.getName());
        }
        if (StringUtils.isEmpty(getStorageDomain().getStorageStaticData().getDescription())) {
            getStorageDomain().getStorageStaticData().setDescription(domainFromIrs.getDescription());
        }
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
            returnValue = StringUtils.equals(domainFromIrs.getConnection().getConnection(), getStorageDomain()
                    .getStorageStaticData().getConnection().getConnection());
        } else if (!StringUtils.isEmpty(getStorageDomain().getStorageStaticData().getStorage())
                && !StringUtils.isEmpty(domainFromIrs.getStorage())) {
            returnValue = StringUtils.equals(domainFromIrs.getStorage(), getStorageDomain().getStorageStaticData()
                    .getStorage());
        }
        if (!returnValue) {
            addValidationMessage(EngineMessage.ERROR_CANNOT_ADD_EXISTING_STORAGE_DOMAIN_CONNECTION_DATA_ILLEGAL);
        }
        return returnValue;
    }
}
