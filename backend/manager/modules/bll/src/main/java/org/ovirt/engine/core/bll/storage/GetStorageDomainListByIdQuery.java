package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetStorageDomainListByIdQuery<P extends StorageDomainQueryParametersBase> extends QueriesCommandBase<P> {
    public GetStorageDomainListByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<StorageDomain> result = DbFacade.getInstance().getStorageDomainDao().getAllForStorageDomain(
                getParameters().getStorageDomainId());
        java.util.ArrayList<StorageDomain> temp = new java.util.ArrayList<StorageDomain>(result);
        for (StorageDomain domain : temp) {
            if (domain.getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached) {
                result.remove(domain);
            }
        }
        getQueryReturnValue().setReturnValue(result);
    }
}
