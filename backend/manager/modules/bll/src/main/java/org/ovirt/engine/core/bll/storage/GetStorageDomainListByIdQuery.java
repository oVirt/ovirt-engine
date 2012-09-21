package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetStorageDomainListByIdQuery<P extends StorageDomainQueryParametersBase> extends QueriesCommandBase<P> {
    public GetStorageDomainListByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<storage_domains> result = DbFacade.getInstance().getStorageDomainDao().getAllForStorageDomain(
                getParameters().getStorageDomainId());
        java.util.ArrayList<storage_domains> temp = new java.util.ArrayList<storage_domains>(result);
        for (storage_domains domain : temp) {
            if (domain.getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached) {
                result.remove(domain);
            }
        }
        getQueryReturnValue().setReturnValue(result);
    }
}
