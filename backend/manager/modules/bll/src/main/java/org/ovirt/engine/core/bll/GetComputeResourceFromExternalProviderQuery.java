package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;

public class GetComputeResourceFromExternalProviderQuery<P extends ProviderQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetComputeResourceFromExternalProviderQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Provider hostProvider = getParameters().getProvider();
        List<ExternalComputeResource> providerComputeResources = getProviderComputeResource(hostProvider);
        getQueryReturnValue().setReturnValue(providerComputeResources);
    }

    protected List<ExternalComputeResource> getProviderComputeResource(Provider hostProvider) {
        HostProviderProxy proxy = providerProxyFactory.create(hostProvider);
        return proxy.getComputeResources();
    }
}
