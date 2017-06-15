package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.NetworkUtils;

public class RemoveNetworkParametersBuilder extends HostSetupNetworksParametersBuilder {

    private final ManagementNetworkUtil managementNetworkUtil;

    @Inject
    public RemoveNetworkParametersBuilder(ManagementNetworkUtil managementNetworkUtil,
            InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao) {
        super(interfaceDao, vdsStaticDao, networkClusterDao, networkAttachmentDao);
        Validate.notNull(managementNetworkUtil, "managementNetworkUtil cannot be null");
        this.managementNetworkUtil = managementNetworkUtil;
    }

    public ArrayList<ActionParametersBase> buildParameters(Network network, List<VdsNetworkInterface> labeledNics) {
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();

        if (managementNetworkUtil.isManagementNetwork(network.getId())) {
            return parameters;
        }

        for (VdsNetworkInterface nic : labeledNics) {
            PersistentHostSetupNetworksParameters setupNetworkParams =
                    createHostSetupNetworksParameters(nic.getVdsId());
            setupNetworkParams.setNetworkNames(network.getName());

            Map<String, VdsNetworkInterface> nicByNetworkName =
                    NetworkUtils.hostInterfacesByNetworkName(getNics(nic.getVdsId()));
            VdsNetworkInterface nicToConfigure = getNicToConfigure(getNics(nic.getVdsId()), nic.getId());

            if (nicToConfigure == null) {
                throw new EngineException(EngineError.LABELED_NETWORK_INTERFACE_NOT_FOUND);
            }

            NetworkAttachment networkAttachment = getNetworkIdToAttachmentMap(nic.getVdsId()).get(network.getId());

            if (networkAttachment != null) {
                if (networkAttachment.getNicId().equals(nicToConfigure.getId())) {
                    setupNetworkParams.getRemovedNetworkAttachments().add(networkAttachment.getId());
                }
            } else {
                VdsNetworkInterface nicWithNetwork = nicByNetworkName.get(network.getName());

                if (nicWithNetwork != null && NetworkCommonUtils.stripVlan(nicWithNetwork).equals(nic.getName())) {
                    setupNetworkParams.getRemovedUnmanagedNetworks().add(network.getName());
                }
            }
            parameters.add(setupNetworkParams);
        }

        return parameters;
    }
}
