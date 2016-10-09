package org.ovirt.engine.core.bll.exportimport;

import java.util.Collection;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VnicProfileDao;

@Singleton
public class TargetVnicProfileFinder {

    private final VnicProfileDao vnicProfileDao;

    @Inject
    TargetVnicProfileFinder(VnicProfileDao vnicProfileDao) {
        this.vnicProfileDao = Objects.requireNonNull(vnicProfileDao);
    }

    public VnicProfile findTargetVnicProfile(
            String networkName,
            String vnicProfileName,
            Collection<ExternalVnicProfileMapping> externalVnicProfileMappings) {
        final Guid targetVnicProfileId = externalVnicProfileMappings
                .stream()
                .filter(mapping ->
                        mapping.getExternalNetworkName().equals(networkName)
                                && mapping.getExternalNetworkProfileName().equals(vnicProfileName))
                .findFirst()
                .map(ExternalVnicProfileMapping::getVnicProfileId)
                .orElse(null);
        if (targetVnicProfileId == null) {
            return null;
        }

        return vnicProfileDao.get(targetVnicProfileId);
    }
}
