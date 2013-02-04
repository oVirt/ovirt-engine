package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;

public class AttachStorageDomainVDSCommand<P extends AttachStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public AttachStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        status = getIrsProxy().attachStorageDomain(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString());
        ProceedProxyReturnValue();
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        storage_domains domainFromDb = DbFacade.getInstance().getStorageDomainDao().get(getParameters().getStorageDomainId());
        if (domainFromDb == null || domainFromDb.getstorage_domain_type() == StorageDomainType.ImportExport) {
            return new IrsOperationFailedNoFailoverException(errorMessage);
        }
        return super.createDefaultConcreteException(errorMessage);
    }
}
