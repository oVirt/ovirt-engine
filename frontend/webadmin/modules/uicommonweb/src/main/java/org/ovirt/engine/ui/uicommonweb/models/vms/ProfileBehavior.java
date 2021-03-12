package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public abstract class ProfileBehavior {

    private List<Network> clusterNetworks = new ArrayList<>();
    private String managementNetworkName;

    public void initProfiles(final Guid clusterId,
            final Guid dcId,
            final AsyncQuery<List<VnicProfileView>> profilesQuery) {

        AsyncQuery<QueryReturnValue> networksQuery = new AsyncQuery<>(response -> {
            clusterNetworks = response.getReturnValue();
            managementNetworkName = clusterNetworks.stream()
                    .filter(n -> n.getCluster().isManagement())
                    .map(Network::getName)
                    .findFirst()
                    .orElse(null);

            profilesQuery.converterCallback = returnValue -> {
                List<VnicProfileView> vnicProfiles = new ArrayList<>();
                vnicProfiles.add(VnicProfileView.EMPTY);

                if (returnValue == null) {
                    return vnicProfiles;
                }

                for (VnicProfileView vnicProfile : (List<VnicProfileView>) returnValue) {
                    Network network = findNetworkById(vnicProfile.getNetworkId());
                    if (network != null) {
                        vnicProfiles.add(vnicProfile);
                        updateDescriptionForExternalNetwork(network, vnicProfile);
                    }
                    updateDescriptionForFailoverVnicProfile(vnicProfile);

                }

                Collections.sort(vnicProfiles, Linq.VnicProfileViewComparator);

                return vnicProfiles;
            };
            AsyncDataProvider.getInstance().getVnicProfilesByDcId(profilesQuery, dcId);
        });
        Frontend.getInstance()
                .runQuery(QueryType.GetAllVmNetworksByClusterId,
                        new IdQueryParameters(clusterId),
                        networksQuery);
    }

    public abstract void initSelectedProfile(ListModel<VnicProfileView> profileLists,
            VmNetworkInterface networkInterface);

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

    private void updateDescriptionForExternalNetwork(Network network, VnicProfileView vnicProfileView) {
        if (network.isExternal()) {
            UIConstants constants = ConstantsManager.getInstance().getConstants();
            updateDescription(vnicProfileView, constants.externalNetworkInfo());
        }
    }

    private void updateDescriptionForFailoverVnicProfile(VnicProfileView vnicProfileView) {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        String failoverName = vnicProfileView.getFailoverVnicProfileName();
        if (failoverName != null) {
            updateDescription(vnicProfileView, constants.failoverVnicProfile() + ": " + failoverName); //$NON-NLS-1$
        }
    }

    private void updateDescription(VnicProfileView vnicProfileView, String descUpdate) {
        String description = vnicProfileView.getDescription();
        vnicProfileView.setDescription(description != null ? description + " - " + descUpdate : descUpdate); //$NON-NLS-1$
    }
}
