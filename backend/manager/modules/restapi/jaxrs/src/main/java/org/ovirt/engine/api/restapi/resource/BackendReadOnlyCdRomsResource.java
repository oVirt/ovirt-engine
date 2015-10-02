package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.types.CdRomMapper.CDROM_ID;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cdroms;
import org.ovirt.engine.api.resource.ReadOnlyDevicesResource;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendReadOnlyCdRomsResource<Q extends IVdcQueryable>
        extends AbstractBackendReadOnlyDevicesResource<Cdrom, Cdroms, Q>
        implements ReadOnlyDevicesResource<Cdrom, Cdroms> {

    public BackendReadOnlyCdRomsResource(Class<Q> entityType, Guid parentId, VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        super(Cdrom.class, Cdroms.class, entityType, parentId, queryType, queryParams);
    }

    @Override
    protected boolean validate(Cdrom cdrom) {
        return cdrom.isSetFile() && cdrom.getFile().isSetId() && !"".equals(cdrom.getFile().getId());
    }

    @Override
    protected <T> boolean matchEntity(Q entity, T id) {
        return id.equals(CDROM_ID) && parentId.equals(entity.getQueryableId());
    }
}
