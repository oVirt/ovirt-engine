package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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

    protected boolean checkExistingStorageDomain() {
        if (DbFacade.getInstance().getStorageDomainStaticDao().get(getStorageDomain().getId()) != null) {
            return  failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }

        List<Guid> storageIds = (ArrayList<Guid>) runVdsCommand(VDSCommandType.HSMGetStorageDomainsList,
                new HSMGetStorageDomainsListVDSCommandParameters(getVdsId(), Guid.Empty, getStorageDomain()
                        .getStorageType(), getStorageDomain().getStorageDomainType(), "")
        ).getReturnValue();

        if (!storageIds.contains(getStorageDomain().getId())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
        }

        Pair<StorageDomainStatic, Guid> domainFromIrs = (Pair<StorageDomainStatic, Guid>) runVdsCommand(
                VDSCommandType.HSMGetStorageDomainInfo,
                new HSMGetStorageDomainInfoVDSCommandParameters(getVdsId(), getStorageDomain().getId())
        ).getReturnValue();

        if (domainFromIrs.getFirst().getStorageDomainType() != getStorageDomain().getStorageDomainType()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_CHANGE_STORAGE_DOMAIN_TYPE);
        }

        return concreteCheckExistingStorageDomain(domainFromIrs);
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
