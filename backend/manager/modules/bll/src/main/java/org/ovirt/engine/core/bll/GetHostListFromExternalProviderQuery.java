package org.ovirt.engine.core.bll;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;


public class GetHostListFromExternalProviderQuery<P extends GetHostListFromExternalProviderParameters> extends QueriesCommandBase<P> {
    @Inject
    private ProviderDao providerDao;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetHostListFromExternalProviderQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Provider hostProvider = getProvider();
        List<VDS> providerHosts = getProviderHosts(hostProvider, getParameters().getSearchFilter());
        if (providerHosts != null && getParameters().isFilterOutExistingHosts()) {
            filterOutExistingHosts(providerHosts);
        }
        getQueryReturnValue().setReturnValue(providerHosts);
    }

    private void filterOutExistingHosts(List<VDS> hosts) {
        List<VDS> existingHosts = getExistingHosts();
        Set<String> existingHostNames = hostNameSet(existingHosts);
        Iterator<VDS> hostIter = hosts.listIterator();
        while (hostIter.hasNext()) {
            if (existingHostNames.contains(hostIter.next().getHostName())) {
                hostIter.remove();
            }
        }
    }

    private Set<String> hostNameSet(List<VDS> hosts) {
        return hosts.stream().map(VDS::getHostName).collect(Collectors.toSet());
    }

    protected List<VDS> getProviderHosts(Provider hostProvider, String searchFilter) {
        HostProviderProxy proxy = providerProxyFactory.create(hostProvider);
        if (StringUtils.isEmpty(searchFilter)) {
            return proxy.getAll();
        } else {
            return proxy.getFiltered(searchFilter);
        }
    }

    protected List<VDS> getExistingHosts() {
        return vdsDao.getAll();
    }

    protected Provider getProvider() {
        return providerDao.get(getParameters().getProviderId());
    }

}
