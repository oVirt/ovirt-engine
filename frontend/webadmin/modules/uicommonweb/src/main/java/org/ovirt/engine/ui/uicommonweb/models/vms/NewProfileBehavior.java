package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class NewProfileBehavior extends ProfileBehavior {

    @Override
    public void initSelectedProfile(ListModel<VnicProfileView> profileList, VmNetworkInterface networkInterface) {
        Collection<VnicProfileView> profiles =
                Optional.ofNullable(profileList.getItems()).orElse(Collections.emptyList());

        Stream<VnicProfileView> profileStream = profiles.stream()
                .filter(profile -> !Objects.equals(profile, VnicProfileView.EMPTY));

        if (StringHelper.isNotNullOrEmpty(getManagementNetworkName())) {
            profileStream = profileStream.filter(profile -> Objects.equals(profile.getNetworkName(),
                    getManagementNetworkName()));
        }

        profileList.setSelectedItem(profileStream.findFirst().orElse(VnicProfileView.EMPTY));
    }

}
