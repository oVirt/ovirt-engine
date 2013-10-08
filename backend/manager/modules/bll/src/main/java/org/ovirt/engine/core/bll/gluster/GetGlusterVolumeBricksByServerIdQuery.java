package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * Query to fetch a list of gluster volume bricks associated with given serverId
 */
public class GetGlusterVolumeBricksByServerIdQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeBricksByServerIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getGlusterBrickDao().getGlusterVolumeBricksByServerId(getParameters().getId()));
    }
}
