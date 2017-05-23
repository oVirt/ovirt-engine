package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;

public class GetHostGroupsFromExternalProviderQuery<P extends ProviderQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetHostGroupsFromExternalProviderQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Provider hostProvider = getParameters().getProvider();
        List<ExternalHostGroup> providerHostGroups = getProviderHostGroups(hostProvider);
        getQueryReturnValue().setReturnValue(providerHostGroups);
    }

    protected List<ExternalHostGroup> getProviderHostGroups(Provider hostProvider) {
        HostProviderProxy proxy = providerProxyFactory.create(hostProvider);
        return proxy.getHostGroups();
    }
}
