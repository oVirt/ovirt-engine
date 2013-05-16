package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookQueryParameters;

/**
 * Query to fetch a {@link GlusterHookEntity} given the hook ID
 */
public class GetGlusterHookByIdQuery<P extends GlusterHookQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterHookByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        GlusterHookEntity hookEntity = getGlusterHookDao().getById(getParameters().getHookId());
        if (hookEntity != null && getParameters().isIncludeServerHooks()) {
            hookEntity.setServerHooks(getGlusterHookDao().getGlusterServerHooks(getParameters().getHookId()));
        }
        getQueryReturnValue().setReturnValue(hookEntity);
    }
}
