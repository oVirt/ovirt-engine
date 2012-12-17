package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * Query to fetch a single gluster volume given the volume ID
 */
public class GetGlusterVolumeByIdQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getGlusterVolumeDao().getById(getParameters().getId()));
    }
}
