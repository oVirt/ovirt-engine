package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * Query to fetch a single gluster volume given the gluster task id
 */
public class GetGlusterVolumeByTaskIdQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeByTaskIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getGlusterVolumeDao().getVolumeByGlusterTask(getParameters().getId()));
    }
}
