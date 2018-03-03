package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.vdscommands.AttachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;

public class AttachStorageDomainVDSCommand<P extends AttachStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public AttachStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        status = getIrsProxy().attachStorageDomain(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        StorageDomain domainFromDb = storageDomainDao.get(getParameters().getStorageDomainId());
        if (domainFromDb == null || !domainFromDb.getStorageDomainType().isDataDomain()) {
            return new IrsOperationFailedNoFailoverException(errorMessage);
        }
        return super.createDefaultConcreteException(errorMessage);
    }
}
