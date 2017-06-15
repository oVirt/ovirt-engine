package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.NetworkUtils;

public class AddNetworkParametersBuilder extends HostSetupNetworksParametersBuilder {

    @Inject
    public AddNetworkParametersBuilder(InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao) {
        super(interfaceDao, vdsStaticDao, networkClusterDao, networkAttachmentDao);
    }

    public ArrayList<ActionParametersBase> buildParameters(Network network, List<VdsNetworkInterface> nics) {
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();

        for (VdsNetworkInterface nic : nics) {
            PersistentHostSetupNetworksParameters setupNetworkParams =
                    createHostSetupNetworksParameters(nic.getVdsId());
            setupNetworkParams.setNetworkNames(network.getName());
            VdsNetworkInterface nicToConfigure = getNicToConfigure(getNics(nic.getVdsId()), nic.getId());
            if (nicToConfigure == null) {
                throw new EngineException(EngineError.LABELED_NETWORK_INTERFACE_NOT_FOUND);
            }

            NetworkAttachment networkAttachment =
                    new NetworkAttachment(nicToConfigure,
                            network,
                            NetworkUtils.createIpConfigurationFromVdsNetworkInterface(getVlanDevice(nicToConfigure,
                                    network.getVlanId())));

            setupNetworkParams.getNetworkAttachments().add(networkAttachment);
            addBootProtocolForRoleNetworkAttachment(nicToConfigure, network, networkAttachment);

            parameters.add(setupNetworkParams);
        }

        return parameters;
    }
}
