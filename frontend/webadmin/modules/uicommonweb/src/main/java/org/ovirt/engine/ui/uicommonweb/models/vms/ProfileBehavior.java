package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.IAsyncConverter;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public abstract class ProfileBehavior {

    private List<Network> clusterNetworks = new ArrayList<>();
    private String managementNetworkName;

    public void initProfiles(final Guid clusterId,
            final Guid dcId,
            final AsyncQuery profilesQuery) {

        profilesQuery.converterCallback = new IAsyncConverter() {

            @Override
            public Object convert(Object returnValue, AsyncQuery asyncQuery) {
                List<Network> clusterNetworks = (ArrayList<Network>) asyncQuery.getModel();

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

                Collections.sort(vnicProfiles, new Linq.VnicProfileViewComparator());

                return vnicProfiles;
            }
        };

        AsyncQuery networksQuery = new AsyncQuery();
        networksQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model1, Object result1) {
                List<Network> clusterNetworks = (List<Network>) result1;
                Network managementNetwork = Linq.findManagementNetwork(clusterNetworks);
                managementNetworkName = managementNetwork != null ? managementNetwork.getName() : null;

                profilesQuery.setModel(clusterNetworks);
                AsyncDataProvider.getInstance().getVnicProfilesByDcId(profilesQuery, dcId);
            }
        };
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
