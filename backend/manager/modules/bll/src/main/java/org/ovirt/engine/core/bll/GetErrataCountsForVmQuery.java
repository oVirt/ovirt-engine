package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.ErrataCounts;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetErrataCountsForVmQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    @Inject
    private ProviderDao providerDao;

    public GetErrataCountsForVmQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = vmDao.get(getParameters().getId());
        if (vm == null) {
            failWith(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return;
        }

        Provider<?> provider = getHostProvider(vm);
        if (provider == null) {
            failWith(EngineMessage.NO_FOREMAN_PROVIDER_FOR_VM);
            return;
        }

        HostProviderProxy proxy = getHostProviderProxy(provider);
        List<Erratum> errata = proxy.getErrataForHost(vm.getVmHost()); // vm.getVmHost() == vm's hostname
        ErrataCounts stats = new ErrataCounts();
        for (Erratum erratum : errata) {
            stats.addToCounts(erratum);
        }

        setReturnValue(stats);
    }

    private void failWith(EngineMessage failure) {
        getQueryReturnValue().setExceptionString(failure.name());
        getQueryReturnValue().setSucceeded(false);
    }

    HostProviderProxy getHostProviderProxy(Provider<?> provider) {
        return (HostProviderProxy) ProviderProxyFactory.getInstance().create(provider);
    }

    private Provider<?> getHostProvider(VM vm) {
        return vm.getProviderId() == null ? null : providerDao.get(vm.getProviderId());
    }
}
