package org.ovirt.engine.core.bll.storage;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AddExistingSANStorageDomainCommand<T extends AddSANStorageDomainParameters> extends
        AddSANStorageDomainCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddExistingSANStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddExistingSANStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        AddStorageDomainInDb();
        ProceedVGLunsInDb();
        UpdateStorageDomainDynamicFromIrs();
        setSucceeded(true);
    }

    @Override
    protected boolean CanAddDomain() {
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.ImportExport) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_EXISTING_STORAGE_DOMAIN_CONNECTION_DATA_ILLEGAL);
            return false;
        }

        return CheckExistingStorageDomain();
    }

    @Override
    protected boolean ConcreteCheckExistingStorageDomain(Pair<StorageDomainStatic, SANState> domainFromIrs) {
        boolean returnValue = false;
        if (StringUtils.isNotEmpty(getStorageDomain().getStorageStaticData().getStorage())
                && StringUtils.isNotEmpty(domainFromIrs.getFirst().getStorage())) {
            returnValue =
                    (StringUtils.equals(domainFromIrs.getFirst().getStorage(), getStorageDomain().getStorageStaticData()
                            .getStorage()));
        }
        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_EXISTING_STORAGE_DOMAIN_CONNECTION_DATA_ILLEGAL);
        } else if (domainFromIrs.getSecond() != null && SANState.OK != domainFromIrs.getSecond()) {
            returnValue = false;
            getReturnValue().getCanDoActionMessages().add(
                    VdcBllMessages.ERROR_CANNOT_ADD_EXISTING_STORAGE_DOMAIN_LUNS_PROBLEM.toString());
        }

        return returnValue;
    }
}
