package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
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
        StorageDomain domainFromDb = DbFacade.getInstance().getStorageDomainDao().get(getParameters().getStorageDomainId());
        if (domainFromDb == null || domainFromDb.getStorageDomainType() == StorageDomainType.ImportExport) {
            return new IrsOperationFailedNoFailoverException(errorMessage);
        }
        return super.createDefaultConcreteException(errorMessage);
    }
}
