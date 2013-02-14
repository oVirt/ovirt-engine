package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.storage.GetStorageDomainsByVmTemplateIdQuery;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.GetPermittedStorageDomainsByTemplateIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetPermittedStorageDomainsByTemplateIdQuery<P extends GetPermittedStorageDomainsByTemplateIdParameters>
        extends GetStorageDomainsByVmTemplateIdQuery<P> {

    public GetPermittedStorageDomainsByTemplateIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected StorageDomain getStorageDomain(Guid domainId) {
        return DbFacade.getInstance()
                .getStorageDomainDao()
                .getPermittedStorageDomainsById(getUserID(), getParameters().getActionGroup(), domainId);
    }

}
