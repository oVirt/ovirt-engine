package org.ovirt.engine.core.bll;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class GetHostListFromExternalProviderQuery<P extends GetHostListFromExternalProviderParameters> extends QueriesCommandBase<P> {
    public GetHostListFromExternalProviderQuery(P parameters) {
        super(parameters);
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
        HostProviderProxy proxy = ProviderProxyFactory.getInstance().create(hostProvider);
        if (StringUtils.isEmpty(searchFilter)) {
            return proxy.getAll();
        } else {
            return proxy.getFiltered(searchFilter);
        }
    }

    protected List<VDS> getExistingHosts() {
        return DbFacade.getInstance().getVdsDao().getAll();
    }

    protected Provider getProvider() {
        return DbFacade.getInstance().getProviderDao().get(getParameters().getProviderId());
    }

}
