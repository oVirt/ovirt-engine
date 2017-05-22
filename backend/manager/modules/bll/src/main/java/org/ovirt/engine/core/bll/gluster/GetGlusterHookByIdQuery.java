package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookQueryParameters;

/**
 * Query to fetch a {@link GlusterHookEntity} given the hook ID
 */
public class GetGlusterHookByIdQuery<P extends GlusterHookQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterHookByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        GlusterHookEntity hookEntity = glusterHooksDao.getById(getParameters().getHookId());
        if (hookEntity != null && getParameters().isIncludeServerHooks()) {
            hookEntity.setServerHooks(glusterHooksDao.getGlusterServerHooks(getParameters().getHookId()));
        }
        getQueryReturnValue().setReturnValue(hookEntity);
    }
}
