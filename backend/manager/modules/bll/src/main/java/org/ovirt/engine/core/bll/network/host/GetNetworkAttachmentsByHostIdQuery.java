package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.DnsResolverConfigurationDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetNetworkAttachmentsByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private ReportedConfigurationsFiller reportedConfigurationsFiller;

    @Inject
    private InterfaceDao interfaceDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private DnsResolverConfigurationDao dnsResolverConfigurationDao;

    @Inject
    private VdsDao hostDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private NetworkIdNetworkNameCompleter networkIdNetworkNameCompleter;

    public GetNetworkAttachmentsByHostIdQuery(P parameters, EngineContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid hostId = getParameters().getId();

        List<NetworkAttachment> networkAttachments = networkAttachmentDao.getAllForHost(hostId);

        List<VdsNetworkInterface> allInterfacesForHost = interfaceDao.getAllInterfacesForVds(hostId);

        VDS vds = hostDao.get(hostId);
        Guid clusterId = vds.getClusterId();

        BusinessEntityMap<Network> networkMap = new BusinessEntityMap<>(networkDao.getAllForCluster(clusterId));

        reportedConfigurationsFiller.fillReportedConfigurations(allInterfacesForHost,
                networkMap,
                networkAttachments,
                dnsResolverConfigurationDao.get(hostId), clusterId);

        completeNicNames(networkAttachments, allInterfacesForHost);
        completeNetworkNames(networkAttachments, networkMap);

        getQueryReturnValue().setReturnValue(networkAttachments);
    }

    private void completeNicNames(List<NetworkAttachment> attachments, List<VdsNetworkInterface> allInterfacesForHost) {
        NicNameNicIdCompleter nicNameNicIdCompleter = new NicNameNicIdCompleter(allInterfacesForHost);
        nicNameNicIdCompleter.completeNetworkAttachments(attachments);
    }

    private void completeNetworkNames(List<NetworkAttachment> attachments, BusinessEntityMap<Network> networkMap) {
        Guid hostId = getParameters().getId();
        VDS host = hostDao.get(hostId);

        networkIdNetworkNameCompleter.completeNetworkAttachments(
                attachments,
                networkMap,
                host.getStoragePoolId());
    }
}
