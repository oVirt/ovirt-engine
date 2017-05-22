package org.ovirt.engine.core.bll.storage.domain;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;

public abstract class GetUnregisteredEntitiesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;

    @Inject
    protected OvfHelper ovfHelper;

    public GetUnregisteredEntitiesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    protected List<OvfEntityData> getOvfEntityList(VmEntityType vmEntityType) {
        return unregisteredOVFDataDao.getAllForStorageDomainByEntityType(getParameters().getId(), vmEntityType);
    }

}
