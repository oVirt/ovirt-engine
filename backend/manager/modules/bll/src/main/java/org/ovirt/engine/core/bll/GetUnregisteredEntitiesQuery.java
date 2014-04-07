package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.UnregisteredEntitiesQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class GetUnregisteredEntitiesQuery<P extends UnregisteredEntitiesQueryParameters> extends QueriesCommandBase<P> {

    public GetUnregisteredEntitiesQuery(P parameters) {
        super(parameters);
    }

    protected List<OvfEntityData> getOvfEntityList(VmEntityType vmEntityType) {
        List<OvfEntityData> entityList;
        if (getParameters().getEntityGuidList() == null) {
            entityList =
                    getDbFacade().getUnregisteredOVFDataDao()
                            .getAllForStorageDomainByEntityType(getParameters().getId(), vmEntityType);
        } else {
            entityList = new ArrayList<OvfEntityData>();
            for (Guid entityGuid : getParameters().getEntityGuidList()) {
                OvfEntityData ovf =
                        getDbFacade().getUnregisteredOVFDataDao().getByEntityIdAndStorageDomain(entityGuid,
                                getParameters().getId());
                entityList.add(ovf);
            }
        }
        return entityList;
    }

    protected OvfHelper getOvfHelper() {
        return new OvfHelper();
    }
}
