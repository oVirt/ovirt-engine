package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.gluster.GetGlusterVolumeByIdQueryParameters;

/**
 * Query to fetch a gluster volume bricks of given the volume ID
 */
public class GetGlusterVolumeBricksQuery<P extends GetGlusterVolumeByIdQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeBricksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getGlusterBrickDao().getBricksOfVolume(getParameters().getVolumeId()));
    }
}
