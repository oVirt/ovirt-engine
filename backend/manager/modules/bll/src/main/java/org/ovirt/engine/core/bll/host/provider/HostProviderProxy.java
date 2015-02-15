package org.ovirt.engine.core.bll.host.provider;

import java.util.List;

import org.ovirt.engine.core.bll.host.provider.foreman.ContentHost;
import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;

public interface HostProviderProxy extends ProviderProxy {

    List<VDS> getAll();
    List<VDS> getFiltered(String filter);
    List<ExternalDiscoveredHost> getDiscoveredHosts();
    List<ExternalHostGroup> getHostGroups();
    List<ExternalComputeResource> getComputeResources();
    List<Erratum> getErrataForHost(VdsStatic host);
    Erratum getErratumForHost(VdsStatic host, String erratumId);
    ContentHost findContentHost(VdsStatic host);

    void provisionHost(VDS vds,
                       ExternalHostGroup hg,
                       ExternalComputeResource computeResource,
                       String mac,
                       String discoverName,
                       String rootPassword,
                       String ip);

}
