package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GetStorageDomainListByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private StorageDomainDao storageDomainDao;

    public GetStorageDomainListByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<StorageDomain> result = storageDomainDao.getAllForStorageDomain(getParameters().getId());
        ArrayList<StorageDomain> temp = new ArrayList<>(result);
        for (StorageDomain domain : temp) {
            if (domain.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached) {
                result.remove(domain);
            }
        }
        getQueryReturnValue().setReturnValue(result);
    }
}
