package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.ContentHostIdentifier;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.HostErratumQueryParameters;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetErratumByIdForVmQuery<P extends HostErratumQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetErratumByIdForVmQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = vmDao.get(getParameters().getId());
        if (vm == null || vm.getProviderId() == null) {
            return;
        }

        Provider<?> provider = providerDao.get(vm.getProviderId());
        if (provider != null) {
            HostProviderProxy proxy = providerProxyFactory.create(provider);
            ContentHostIdentifier contentHostIdentifier = ContentHostIdentifier.builder()
                    .withId(vm.getId())
                    .withFqdn(vm.getDynamicData().getFqdn())
                    .withName(vm.getName())
                    .build();

            getQueryReturnValue()
                    .setReturnValue(proxy.getErratumForHost(contentHostIdentifier, getParameters().getErratumId()));
        }
    }
}
