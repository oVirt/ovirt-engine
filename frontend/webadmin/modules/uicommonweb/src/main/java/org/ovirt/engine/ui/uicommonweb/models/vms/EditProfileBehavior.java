package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class EditProfileBehavior extends ProfileBehavior {

    @Override
    public void initSelectedProfile(ListModel<VnicProfileView> profileList, VmNetworkInterface networkInterface) {
        if (networkInterface.getVnicProfileId() != null) {
            Collection<VnicProfileView> profiles =
                    Optional.ofNullable(profileList.getItems()).orElse(Collections.emptyList());
            profileList.setSelectedItem(profiles.stream()
                    .filter(profile -> Objects.equals(profile.getId(), networkInterface.getVnicProfileId()))
                    .findAny()
                    .orElse(VnicProfileView.EMPTY));
        } else {
            profileList.setSelectedItem(VnicProfileView.EMPTY);
        }
    }

}
