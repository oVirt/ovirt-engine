package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class EditProfileBehavior extends ProfileBehavior {

    @Override
    public void initSelectedProfile(ListModel profileList, VmNetworkInterface networkInterface) {
        List<VnicProfileView> profiles = (List<VnicProfileView>) profileList.getItems();
        profiles = profiles == null ? new ArrayList<VnicProfileView>() : profiles;

        if (networkInterface.getVnicProfileId() == null) {
            profileList.setSelectedItem(VnicProfileView.EMPTY);
            return;
        }

        for (VnicProfileView vnicProfile : profiles) {
            Guid profileId = vnicProfile == null ? null : vnicProfile.getId();
            if (networkInterface.getVnicProfileId().equals(profileId)) {
                profileList.setSelectedItem(vnicProfile);
                return;
            }
        }

        profileList.setSelectedItem(VnicProfileView.EMPTY);
    }

}
