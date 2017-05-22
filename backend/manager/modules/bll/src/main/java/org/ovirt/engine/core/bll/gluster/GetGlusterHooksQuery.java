package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;


/**
 * Query to fetch gluster hooks for the Gluster cluster
 */
public class GetGlusterHooksQuery<P extends GlusterParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterHooksQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(glusterHooksDao.getByClusterId(getParameters().getClusterId()));
        getQueryReturnValue().setSucceeded(true);
    }

}
