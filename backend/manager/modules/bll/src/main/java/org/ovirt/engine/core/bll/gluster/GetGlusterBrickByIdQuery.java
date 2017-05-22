package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * Query to fetch a single gluster brick given the brick ID
 */
public class GetGlusterBrickByIdQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterBrickByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(glusterBrickDao.getById(getParameters().getId()));
    }
}
