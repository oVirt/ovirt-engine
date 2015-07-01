package org.ovirt.engine.core.bll;

import java.util.Collections;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetErrataForVmQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    @Inject
    private ProviderDao providerDao;

    public GetErrataForVmQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = vmDao.get(getParameters().getId());
        if (vm == null || vm.getVmHost() == null) {
            getQueryReturnValue().setReturnValue(Collections.<Erratum> emptyList());
            return;
        }

        Provider<?> provider = providerDao.get(vm.getProviderId());
        if (provider != null) {
            HostProviderProxy proxy = (HostProviderProxy) ProviderProxyFactory.getInstance().create(provider);
            getQueryReturnValue().setReturnValue(proxy.getErrataForHost(vm.getVmHost()));
        } else {
            getQueryReturnValue().setReturnValue(Collections.<Erratum> emptyList());
        }
    }
}
