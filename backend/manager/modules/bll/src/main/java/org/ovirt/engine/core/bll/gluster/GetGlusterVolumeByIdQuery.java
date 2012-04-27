package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.gluster.GetGlusterVolumeByIdQueryParameters;

/**
 * Query to fetch a single gluster volume given the volume ID
 */
public class GetGlusterVolumeByIdQuery<P extends GetGlusterVolumeByIdQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getGlusterVolumeDao().getById(getParameters().getVolumeId()));
    }
}
