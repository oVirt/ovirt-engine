package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetGlusterVolumeBricksByTaskIdQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {
    public GetGlusterVolumeBricksByTaskIdQuery(P params) {
        super(params);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getGlusterBrickDao().getGlusterVolumeBricksByTaskId(getParameters().getId()));
    }
}
