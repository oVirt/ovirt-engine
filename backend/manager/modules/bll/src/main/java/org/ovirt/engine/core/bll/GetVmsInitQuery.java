package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;

/**
 * Query set VmInit to VMs *** OR *** Templates
 */
public class GetVmsInitQuery<P extends IdsQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmHandler vmHandler;

    public GetVmsInitQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getIds() != null) {
            getQueryReturnValue().setReturnValue(vmHandler.getVmInitWithoutPasswordByIds(getParameters().getIds()));
        }
    }
}
