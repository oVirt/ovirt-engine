package org.ovirt.engine.core.bll.kubevirt;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

import kubevirt.io.V1NetworkAttachmentDefinition;

@ApplicationScoped
public class NetworkUpdater {

    @Inject
    private Instance<BackendInternal> backend;

    @Inject
    private NetworkClusterDao networkClusterDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    public boolean addNetwork(V1NetworkAttachmentDefinition network, Guid clusterId) {
        String networkName = getNetworkName(network);

        // skip network creation if already exists and assigned to cluster
        if (networkDao.getByNameAndCluster(networkName, clusterId) != null) {
            return false;
        }

        StoragePool dataCenter = storagePoolDao.getForCluster(clusterId);
        List<Network> dcNetworks = networkDao.getAllForDataCenter(dataCenter.getId());

        Network dcNetwork = dcNetworks.stream().filter(n -> n.getName().equals(networkName)).findFirst().orElse(null);
        Guid dcNetworkId = dcNetwork == null ? null : dcNetwork.getId();
        // create network if not exists
        if (dcNetworkId == null) {
            // create network
            dcNetwork = new Network();
            dcNetwork.setName(networkName);
            dcNetwork.setDataCenterId(dataCenter.getId());

            AddNetworkStoragePoolParameters params = new AddNetworkStoragePoolParameters(dataCenter.getId(), dcNetwork);
            params.setVnicProfilePublicUse(true);
            NetworkCluster networkCluster = new NetworkCluster();
            networkCluster.setClusterId(clusterId);
            networkCluster.setRequired(false);
            params.setNetworkClusterList(List.of(networkCluster));
            params.setAsync(false);
            ActionReturnValue returnValue = backend.get().runInternalAction(ActionType.AddNetwork, params);
            dcNetworkId = returnValue.getActionReturnValue();
            dcNetwork.setId(dcNetworkId);
            return returnValue.getSucceeded();
        }

        // attach network to cluster
        ActionReturnValue returnValue = backend.get()
                .runInternalAction(ActionType.AttachNetworkToCluster,
                        new AttachNetworkToClusterParameter(clusterId, dcNetwork));
        return returnValue.getSucceeded();
    }

    public boolean removeNetwork(String networkName, Guid clusterId) {
        // skip network deletion if not assigned to cluster
        Network network = networkDao.getByNameAndCluster(networkName, clusterId);
        if (network == null) {
            return false;
        }

        if (managementNetworkUtil.getDefaultManagementNetworkName().equals(networkName)) {
            return false;
        }

        boolean result = TransactionSupport.executeInNewTransaction(() -> {
            networkClusterDao.remove(clusterId, network.getId());

            // Check the assignment of the network to other clusters: Remove the logical network if no other cluster uses it
            List<NetworkCluster> otherClusterNetworks = networkClusterDao.getAllForNetwork(network.getId());
            if (otherClusterNetworks.isEmpty()) {
                try {
                    networkDao.remove(network.getId());
                } catch (Exception e) {
                    // Ignore. In rare cases of race this might fail (adding same network to a different cluster while
                    // attempting to remove it)
                    return false;
                }
            }
            return true;
        });

        return result;
    }

    public static String getNetworkName(V1NetworkAttachmentDefinition network) {
        return String.format("%s-%s", network.getMetadata().getNamespace(), network.getMetadata().getName());
    }
}
