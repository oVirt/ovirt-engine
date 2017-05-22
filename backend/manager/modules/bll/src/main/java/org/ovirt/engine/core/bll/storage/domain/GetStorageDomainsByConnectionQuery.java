package org.ovirt.engine.core.bll.storage.domain;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByConnectionParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

/**
 * Query to retrieve the storage domains which use the given connection (if none then an empty list is returned) in a
 * specific storage pool.
 */
public class GetStorageDomainsByConnectionQuery<P extends GetStorageDomainsByConnectionParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public GetStorageDomainsByConnectionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid storagePoolId = getParameters().getStoragePoolId();
        String connection = getParameters().getConnection();
        List<StorageDomain> domainsList;

        if (storagePoolId != null) {
            domainsList = storageDomainDao.getAllByStoragePoolAndConnection(storagePoolId, connection);
        } else {
            domainsList = storageDomainDao.getAllForConnection(connection);
        }
        getQueryReturnValue().setReturnValue(domainsList);
    }
}
