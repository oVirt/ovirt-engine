package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.ContentHostIdentifier;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetErrataCountsParameters;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetErrataForVmQuery<P extends GetErrataCountsParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetErrataForVmQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = vmDao.get(getParameters().getId());
        if (vm == null) {
            getQueryReturnValue().setReturnValue(ErrataData.emptyData());
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

            ErrataData errataForVm = proxy.getErrataForHost(contentHostIdentifier, getParameters().getErrataFilter());
            getQueryReturnValue().setReturnValue(errataForVm);
        } else {
            getQueryReturnValue().setReturnValue(ErrataData.emptyData());
        }
    }
}
