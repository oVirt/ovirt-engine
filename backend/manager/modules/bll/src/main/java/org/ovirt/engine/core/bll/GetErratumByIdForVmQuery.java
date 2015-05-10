package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.HostErratumQueryParameters;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetErratumByIdForVmQuery<P extends HostErratumQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDAO vmDao;

    @Inject
    private ProviderDao providerDao;

    public GetErratumByIdForVmQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = vmDao.get(getParameters().getId());
        if (vm == null || vm.getVmHost() == null || vm.getProviderId() == null) {
            return;
        }

        Provider<?> provider = providerDao.get(vm.getProviderId());
        if (provider != null) {
            HostProviderProxy proxy = (HostProviderProxy) ProviderProxyFactory.getInstance().create(provider);
            getQueryReturnValue().setReturnValue(proxy.getErratumForHost(vm.getVmHost(), getParameters().getErratumId()));
        }
    }
}
