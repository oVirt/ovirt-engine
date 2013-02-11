package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class NewNetworkBehavior extends NetworkBehavior {

    public static String ENGINE_NETWORK_NAME =
            (String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    @Override
    public void initSelectedNetwork(ListModel networksList, VmNetworkInterface networkInterface) {
        List<Network> networks = (List<Network>) networksList.getItems();
        networks = networks == null ? new ArrayList<Network>() : networks;
        for (Network network : networks) {
            if (ENGINE_NETWORK_NAME != null && network != null && ENGINE_NETWORK_NAME.equals(network.getName())) {
                networksList.setSelectedItem(network);
                return;
            }
        }
        networksList.setSelectedItem(networks.size() > 0 ? networks.get(0) : null);
    }

}
