package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public abstract class ProfileBehavior {

    private List<Network> clusterNetworks = new ArrayList<>();
    private String managementNetworkName;

    public void initProfiles(final Guid clusterId,
            final Guid dcId,
            final AsyncQuery<List<VnicProfileView>> profilesQuery) {

        AsyncQuery<List<Network>> networksQuery = new AsyncQuery<>(clusterNetworks -> {
            managementNetworkName = clusterNetworks.stream()
                    .filter(n -> n.getCluster().isManagement())
                    .map(Network::getName)
                    .findFirst()
                    .orElse(null);

            profilesQuery.converterCallback = returnValue -> {
                ProfileBehavior.this.clusterNetworks = clusterNetworks;

                List<VnicProfileView> vnicProfiles = new ArrayList<>();
                vnicProfiles.add(VnicProfileView.EMPTY);

                if (returnValue == null) {
                    return vnicProfiles;
                }

                for (VnicProfileView vnicProfile : (List<VnicProfileView>) returnValue) {
                    Network network = findNetworkById(vnicProfile.getNetworkId());
                    if (network != null && network.isVmNetwork()) {
                        vnicProfiles.add(vnicProfile);
                    }

                }

                Collections.sort(vnicProfiles, Linq.VnicProfileViewComparator);

                return vnicProfiles;
            };
            AsyncDataProvider.getInstance().getVnicProfilesByDcId(profilesQuery, dcId);
        });
        AsyncDataProvider.getInstance().getClusterNetworkList(networksQuery, clusterId);
    }

    public abstract void initSelectedProfile(ListModel profileLists, VmNetworkInterface networkInterface);

    public Network findNetworkById(Guid networkId) {
        for (Network network : clusterNetworks) {
            if (network.getId().equals(networkId)) {
                return network;
            }
        }
        return null;
    }

    protected String getManagementNetworkName() {
        return managementNetworkName;
    }
}
