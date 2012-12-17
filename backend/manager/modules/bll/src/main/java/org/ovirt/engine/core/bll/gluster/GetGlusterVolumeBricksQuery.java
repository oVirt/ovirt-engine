package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * Query to fetch a gluster volume bricks of given the volume ID
 */
public class GetGlusterVolumeBricksQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeBricksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getGlusterBrickDao().getBricksOfVolume(getParameters().getId()));
    }
}
