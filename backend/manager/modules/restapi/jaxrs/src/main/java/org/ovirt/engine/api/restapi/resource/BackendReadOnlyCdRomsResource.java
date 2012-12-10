package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.resource.ReadOnlyDevicesResource;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.types.CdRomMapper.CDROM_ID;

public class BackendReadOnlyCdRomsResource<Q extends IVdcQueryable>
        extends AbstractBackendReadOnlyDevicesResource<CdRom, CdRoms, Q>
        implements ReadOnlyDevicesResource<CdRom, CdRoms> {

    public BackendReadOnlyCdRomsResource(Class<Q> entityType, Guid parentId, VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        super(CdRom.class, CdRoms.class, entityType, parentId, queryType, queryParams);
    }

    @Override
    protected boolean validate(CdRom cdrom) {
        return cdrom.isSetFile() && cdrom.getFile().isSetId() && !"".equals(cdrom.getFile().getId());
    }

    @Override
    protected <T> boolean matchEntity(Q entity, T id) {
        return id.equals(CDROM_ID) && parentId.equals(entity.getQueryableId());
    }
}
