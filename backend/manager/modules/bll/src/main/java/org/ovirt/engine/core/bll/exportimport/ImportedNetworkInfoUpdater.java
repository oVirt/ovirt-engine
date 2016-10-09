package org.ovirt.engine.core.bll.exportimport;

import java.util.Collection;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

@Singleton
public class ImportedNetworkInfoUpdater {
    private final TargetVnicProfileFinder targetVnicProfileFinder;
    private final NetworkDao networkDao;

    @Inject
    ImportedNetworkInfoUpdater(TargetVnicProfileFinder targetVnicProfileFinder, NetworkDao networkDao) {
        this.targetVnicProfileFinder = Objects.requireNonNull(targetVnicProfileFinder);
        this.networkDao = Objects.requireNonNull(networkDao);
    }

    public void updateNetworkInfo(VmNetworkInterface vnic,
            Collection<ExternalVnicProfileMapping> externalVnicProfileMappings) {
        VnicProfile targetVnicProfile = targetVnicProfileFinder.findTargetVnicProfile(
                vnic.getNetworkName(),
                vnic.getVnicProfileName(),
                externalVnicProfileMappings);
        if (targetVnicProfile != null) {
            vnic.setVnicProfileId(targetVnicProfile.getId());
            vnic.setNetworkName(getVnicProfileNetworkName(targetVnicProfile.getNetworkId()));
            vnic.setVnicProfileName(targetVnicProfile.getName());
        }
    }

    private String getVnicProfileNetworkName(Guid networkId) {
        final Network network = networkDao.get(networkId);
        return network.getName();
    }
}
