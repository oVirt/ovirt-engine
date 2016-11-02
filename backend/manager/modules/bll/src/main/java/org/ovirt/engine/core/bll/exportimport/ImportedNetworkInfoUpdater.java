package org.ovirt.engine.core.bll.exportimport;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;

@Singleton
public class ImportedNetworkInfoUpdater {
    private final ExternalVnicProfileMappingFinder externalVnicProfileMappingFinder;
    private final NetworkDao networkDao;
    private final VnicProfileDao vnicProfileDao;

    @Inject
    ImportedNetworkInfoUpdater(ExternalVnicProfileMappingFinder externalVnicProfileMappingFinder,
            NetworkDao networkDao,
            VnicProfileDao vnicProfileDao) {
        this.externalVnicProfileMappingFinder = Objects.requireNonNull(externalVnicProfileMappingFinder);
        this.networkDao = Objects.requireNonNull(networkDao);
        this.vnicProfileDao = Objects.requireNonNull(vnicProfileDao);
    }

    public void updateNetworkInfo(VmNetworkInterface vnic,
            Collection<ExternalVnicProfileMapping> externalVnicProfileMappings) {
        final Optional<ExternalVnicProfileMapping> mappingEntry = externalVnicProfileMappingFinder.findMappingEntry(
                vnic.getNetworkName(),
                vnic.getVnicProfileName(),
                externalVnicProfileMappings);
        if (mappingEntry.isPresent()) {
            final Guid vnicProfileId = mappingEntry.get().getVnicProfileId();
            if (vnicProfileId == null) {
                setTargetVnicProfile(vnic, null, null, null);
            } else {
                final VnicProfile vnicProfile = vnicProfileDao.get(vnicProfileId);
                setTargetVnicProfile(vnic,
                        vnicProfile.getId(),
                        vnicProfile.getName(),
                        getVnicProfileNetworkName(vnicProfile.getNetworkId()));
            }
        }
    }

    private void setTargetVnicProfile(VmNetworkInterface vnic,
            Guid vnicProfileId,
            String vnicProfileName,
            String networkName) {
        vnic.setVnicProfileId(vnicProfileId);
        vnic.setNetworkName(networkName);
        vnic.setVnicProfileName(vnicProfileName);
    }

    private String getVnicProfileNetworkName(Guid networkId) {
        final Network network = networkDao.get(networkId);
        return network.getName();
    }
}
