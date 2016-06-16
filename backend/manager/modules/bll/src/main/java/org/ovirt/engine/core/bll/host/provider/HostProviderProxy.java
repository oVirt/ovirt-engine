package org.ovirt.engine.core.bll.host.provider;

import java.util.List;

import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.VDS;

public interface HostProviderProxy extends ProviderProxy, ContentHostProvider {

    List<VDS> getAll();

    List<VDS> getFiltered(String filter);

    List<ExternalDiscoveredHost> getDiscoveredHosts();

    List<ExternalHostGroup> getHostGroups();

    List<ExternalComputeResource> getComputeResources();

    void provisionHost(VDS host,
            ExternalHostGroup hg,
            ExternalComputeResource computeResource,
            String mac,
            String discoverName,
            String rootPassword,
            String ip);
}
