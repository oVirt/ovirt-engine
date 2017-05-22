package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetGlusterVolumeGeoRepSessionByIdQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P>{

    public GetGlusterVolumeGeoRepSessionByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        GlusterGeoRepSession geoRepSession = glusterGeoRepDao.getById(getParameters().getId());
        getQueryReturnValue().setReturnValue(geoRepSession);
    }

}
