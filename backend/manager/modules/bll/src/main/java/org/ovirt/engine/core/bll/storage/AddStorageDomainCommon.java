package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddStorageDomainCommon<T extends StorageDomainManagementParameter> extends AddStorageDomainCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddStorageDomainCommon(Guid commandId) {
        super(commandId);
    }

    public AddStorageDomainCommon(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean CanAddDomain() {
        return checkStorageConnection(getStorageDomain().getStorage());
    }

    @Override
    protected String getStorageArgs() {
        return DbFacade.getInstance()
                .getStorageServerConnectionDao()
                .get(getStorageDomain().getStorage())
                .getconnection();
    }

}
