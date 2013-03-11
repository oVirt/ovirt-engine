package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;


/**
 * Query to fetch gluster hooks for the Gluster cluster
 */
public class GetGlusterHooksQuery<P extends GlusterParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterHooksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getGlusterHookDao().getByClusterId(getParameters().getClusterId()));
        getQueryReturnValue().setSucceeded(true);
    }

}
