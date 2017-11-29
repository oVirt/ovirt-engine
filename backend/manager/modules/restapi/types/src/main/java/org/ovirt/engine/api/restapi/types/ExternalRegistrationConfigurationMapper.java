package org.ovirt.engine.api.restapi.types;

import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.api.model.RegistrationAffinityGroupMappings;
import org.ovirt.engine.api.model.RegistrationAffinityLabelMappings;
import org.ovirt.engine.api.model.RegistrationClusterMappings;
import org.ovirt.engine.api.model.RegistrationConfiguration;
import org.ovirt.engine.api.model.RegistrationDomainMappings;
import org.ovirt.engine.api.model.RegistrationLunMappings;
import org.ovirt.engine.api.model.RegistrationRoleMappings;
import org.ovirt.engine.core.common.action.ImportFromConfParameters;

public class ExternalRegistrationConfigurationMapper {

    public static void mapFromModel(RegistrationConfiguration registrationConfiguration,
            ImportFromConfParameters params) {
        if (registrationConfiguration != null) {
            setParamsWithRegistrationConfigurationMappings(registrationConfiguration, params);
        }
    }

    public static void setParamsWithRegistrationConfigurationMappings(RegistrationConfiguration registrationConfiguration,
            ImportFromConfParameters params) {
        if (registrationConfiguration.getAffinityGroupMappings() != null
                && registrationConfiguration.isSetAffinityGroupMappings()) {
            params.setAffinityGroupMap(mapAffinityGroupMapping(registrationConfiguration.getAffinityGroupMappings()));
        }
        if (registrationConfiguration.getAffinityLabelMappings() != null
                && registrationConfiguration.isSetAffinityLabelMappings()) {
            params.setAffinityLabelMap(mapAffinityLabelMapping(registrationConfiguration.getAffinityLabelMappings()));
        }
        if (registrationConfiguration.getClusterMappings() != null && registrationConfiguration.isSetClusterMappings()) {
            params.setClusterMap(mapClusterMapping(registrationConfiguration.getClusterMappings()));
        }
        if (registrationConfiguration.getLunMappings() != null && registrationConfiguration.isSetLunMappings()) {
            params.setExternalLunMap(mapExternalLunMapping(registrationConfiguration.getLunMappings()));
        }
        if (registrationConfiguration.getRoleMappings() != null && registrationConfiguration.isSetRoleMappings()) {
            params.setRoleMap(mapExternalRoleMapping(registrationConfiguration.getRoleMappings()));
        }
        if (registrationConfiguration.getDomainMappings() != null && registrationConfiguration.isSetDomainMappings()) {
            params.setDomainMap(mapExternalDomainMapping(registrationConfiguration.getDomainMappings()));
        }
    }

    private static Map<String, String> mapAffinityGroupMapping(RegistrationAffinityGroupMappings model) {
        return model.getRegistrationAffinityGroupMappings()
                .stream()
                .collect(Collectors.toMap(
                        registrationMap -> registrationMap.isSetFrom() ? registrationMap.getFrom().getName() : null,
                        registrationMap -> registrationMap.isSetTo() ? AffinityGroupMapper.map(registrationMap.getTo(),
                                null).getName() : null));
    }

    private static Map<String, String> mapAffinityLabelMapping(RegistrationAffinityLabelMappings model) {
        return model.getRegistrationAffinityLabelMappings()
                .stream()
                .collect(Collectors.toMap(
                        registrationMap -> registrationMap.isSetFrom() ? registrationMap.getFrom().getName() : null,
                        registrationMap -> registrationMap.isSetTo() ? AffinityLabelMapper.map(registrationMap.getTo(),
                                null).getName()
                                : null));
    }

    private static Map<String, String> mapClusterMapping(RegistrationClusterMappings model) {
        return model.getRegistrationClusterMappings()
                .stream()
                .collect(Collectors.toMap(
                        registrationMap -> registrationMap.isSetFrom() ? registrationMap.getFrom().getName() : null,
                        registrationMap -> registrationMap.isSetTo() ? ClusterMapper.map(registrationMap.getTo(), null).getName()
                                : null));

    }

    private static Map<String, Object> mapExternalLunMapping(RegistrationLunMappings model) {
        return model.getRegistrationLunMappings()
                .stream()
                .collect(Collectors.toMap(
                        registrationMap -> registrationMap.isSetFrom() ? registrationMap.getFrom().getId() : null,
                        registrationMap -> registrationMap.isSetTo() ? DiskMapper.map(registrationMap.getTo(), null)
                                : null));
    }

    private static Map<String, String> mapExternalRoleMapping(RegistrationRoleMappings model) {
        return model.getRegistrationRoleMappings()
                .stream()
                .collect(Collectors.toMap(
                        registrationMap -> registrationMap.isSetFrom() ? registrationMap.getFrom().getName() : null,
                        registrationMap -> registrationMap.isSetTo() ? RoleMapper.map(registrationMap.getTo(), null).getName()
                                : null));
    }

    private static Map<String, String> mapExternalDomainMapping(RegistrationDomainMappings model) {
        return model.getRegistrationDomainMappings()
                .stream()
                .collect(Collectors.toMap(
                        registrationMap -> registrationMap.isSetFrom() ? registrationMap.getFrom().getName() : null,
                        registrationMap -> registrationMap.isSetTo() ? registrationMap.getTo().getName() : null));
    }
}
