package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class EditNetworkBehavior extends NetworkBehavior {

    @Override
    public void initSelectedNetwork(ListModel networksList, VmNetworkInterface networkInterface) {
        List<Network> networks = (List<Network>) networksList.getItems();
        networks = networks == null ? new ArrayList<Network>() : networks;
        for (Network a : networks)
        {
            String networkName = a == null ? null : a.getName();
            if (StringHelper.stringsEqual(networkName, networkInterface.getNetworkName()))
            {
                networksList.setSelectedItem(a);
                return;
            }
        }

        // In some cases, like importVm the network can be deleted from the nic.
        // In these cases, the network can be null even if NetworkLinking is not supported.
        // If the user doesn't set the network, when he'll try to run the VM or update/hotPlug the nic he will get a
        // canDo.
        if (networkInterface.getNetworkName() == null) {
            networksList.setSelectedItem(null);
        }
    }

}
