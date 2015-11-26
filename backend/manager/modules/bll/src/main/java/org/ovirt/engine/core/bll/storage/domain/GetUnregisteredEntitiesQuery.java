package org.ovirt.engine.core.bll.storage.domain;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public abstract class GetUnregisteredEntitiesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetUnregisteredEntitiesQuery(P parameters) {
        super(parameters);
    }

    protected List<OvfEntityData> getOvfEntityList(VmEntityType vmEntityType) {
        List<OvfEntityData> entityList =
                getDbFacade().getUnregisteredOVFDataDao().getAllForStorageDomainByEntityType(
                        getParameters().getId(), vmEntityType);

        return entityList;
    }

    protected OvfHelper getOvfHelper() {
        return new OvfHelper();
    }
}
