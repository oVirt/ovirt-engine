package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
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

    public GetErrataForVmQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = vmDao.get(getParameters().getId());
        if (vm == null || vm.getDynamicData().getVmHost() == null) {
            getQueryReturnValue().setReturnValue(ErrataData.emptyData());
            return;
        }

        Provider<?> provider = providerDao.get(vm.getProviderId());
        if (provider != null) {
            HostProviderProxy proxy = ProviderProxyFactory.getInstance().create(provider);
            ErrataData errataForVm = proxy.getErrataForHost(vm.getDynamicData().getVmHost(),
                    getParameters().getErrataFilter());
            getQueryReturnValue().setReturnValue(errataForVm);
        } else {
            getQueryReturnValue().setReturnValue(ErrataData.emptyData());
        }
    }
}
