package org.ovirt.engine.core.bll.storage.lease;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetEntitiesWithLeaseByStorageIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmStaticDao vmStaticDao;

    @Inject
    private VmTemplateDao vmTemplateDao;

    public GetEntitiesWithLeaseByStorageIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid domainId = getParameters().getId();
        List<VmBase> entities = new ArrayList<>();
        entities.addAll(vmStaticDao.getAllWithLeaseOnStorageDomain(domainId));
        entities.addAll(vmTemplateDao.getAllWithLeaseOnStorageDomain(domainId));
        getQueryReturnValue().setReturnValue(entities);
    }
}
