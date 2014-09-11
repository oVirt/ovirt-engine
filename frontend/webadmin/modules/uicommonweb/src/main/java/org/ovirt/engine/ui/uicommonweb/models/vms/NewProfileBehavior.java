package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class NewProfileBehavior extends ProfileBehavior {

    public static final String ENGINE_NETWORK_NAME =
            (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.DefaultManagementNetwork);

    @Override
    public void initSelectedProfile(ListModel profileList, VmNetworkInterface networkInterface) {
        List<VnicProfileView> profiles = (List<VnicProfileView>) profileList.getItems();
        profiles = profiles == null ? new ArrayList<VnicProfileView>() : profiles;
        for (VnicProfileView profile : profiles) {
            if (ENGINE_NETWORK_NAME != null && profile != null && ENGINE_NETWORK_NAME.equals(profile.getNetworkName())) {
                profileList.setSelectedItem(profile);
                return;
            }
        }
        profileList.setSelectedItem(profiles.size() > 0 ? profiles.get(0) : VnicProfileView.EMPTY);
    }

}
