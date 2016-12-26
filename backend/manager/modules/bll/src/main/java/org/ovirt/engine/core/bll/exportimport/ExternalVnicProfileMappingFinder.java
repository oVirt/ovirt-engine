package org.ovirt.engine.core.bll.exportimport;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;

@Singleton
public class ExternalVnicProfileMappingFinder {

    /**
     * Finds the mapping entry that matches the given network and vnic profile names.
     *
     * @return {@code Optional} that wraps the entity that matches the input if such is found, or otherwise
     *         {@code Optional.empty}.
     */
    public Optional<ExternalVnicProfileMapping> findMappingEntry(String networkName,
            String vnicProfileName,
            Collection<ExternalVnicProfileMapping> externalVnicProfileMappings) {
        return externalVnicProfileMappings
                .stream()
                .filter(mapping -> Objects.equals(mapping.getExternalNetworkName(), networkName)
                        && Objects.equals(mapping.getExternalNetworkProfileName(), vnicProfileName))
                .findFirst();
    }
}
