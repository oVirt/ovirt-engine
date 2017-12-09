package org.ovirt.engine.api.restapi.types;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.api.model.VnicProfileMapping;
import org.ovirt.engine.api.model.VnicProfileMappings;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.compat.Guid;

@Deprecated
public class ExternalVnicProfileMappingMapper {

    // hide me
    private ExternalVnicProfileMappingMapper() {}

    @Deprecated
    public static Collection<ExternalVnicProfileMapping> mapFromModel(VnicProfileMappings vnicProfileMappings) {
        return isVnicProfileMappingSupplied(vnicProfileMappings)
                ? mapVnicProfileMappings(vnicProfileMappings)
                : Collections.emptyList();
    }

    private static boolean isVnicProfileMappingSupplied(VnicProfileMappings vnicProfileMappings) {
        return vnicProfileMappings != null &&
                vnicProfileMappings.isSetVnicProfileMappings();
    }

    private static List<ExternalVnicProfileMapping> mapVnicProfileMappings(VnicProfileMappings vnicProfileMappings) {
        return vnicProfileMappings.getVnicProfileMappings()
                .stream()
                .map(ExternalVnicProfileMappingMapper::mapSingleMappingEntry)
                .collect(Collectors.toList());
    }

    private static ExternalVnicProfileMapping mapSingleMappingEntry(VnicProfileMapping model) {
        return new ExternalVnicProfileMapping(model.getSourceNetworkName(),
                model.getSourceNetworkProfileName(),
                getTargetVnicProfileId(model));
    }

    private static Guid getTargetVnicProfileId(VnicProfileMapping model) {
        return model.isSetTargetVnicProfile()
                ? Guid.createGuidFromString(model.getTargetVnicProfile().getId())
                //this will set the target vnic profile to <empty> (no profile)
                : null;
    }
}
