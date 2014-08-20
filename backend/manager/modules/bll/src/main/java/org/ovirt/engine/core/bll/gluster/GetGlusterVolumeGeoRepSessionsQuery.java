package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;

public class GetGlusterVolumeGeoRepSessionsQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P>{

    public GetGlusterVolumeGeoRepSessionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        GlusterGeoRepDao geoRepDao = getGeoRepDao();
        getQueryReturnValue().setReturnValue(geoRepDao.getGeoRepSessions(getParameters().getId()));
    }

}
