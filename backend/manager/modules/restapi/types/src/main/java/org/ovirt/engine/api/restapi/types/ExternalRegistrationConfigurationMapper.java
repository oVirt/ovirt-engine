package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.utils.GuidUtils.asGuid;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.RegistrationAffinityGroupMappings;
import org.ovirt.engine.api.model.RegistrationAffinityLabelMappings;
import org.ovirt.engine.api.model.RegistrationClusterMappings;
import org.ovirt.engine.api.model.RegistrationConfiguration;
import org.ovirt.engine.api.model.RegistrationDomainMappings;
import org.ovirt.engine.api.model.RegistrationLunMappings;
import org.ovirt.engine.api.model.RegistrationRoleMappings;
import org.ovirt.engine.api.model.RegistrationVnicProfileMapping;
import org.ovirt.engine.api.model.RegistrationVnicProfileMappings;
import org.ovirt.engine.core.common.action.ImportFromConfParameters;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.compat.Guid;

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
        if (hasVnicProfileMappings(registrationConfiguration)) {
            params.setExternalVnicProfileMappings(
                    mapVnicProfilesMapping(registrationConfiguration.getVnicProfileMappings()));
        }
    }

    private static boolean hasVnicProfileMappings(RegistrationConfiguration registrationConfiguration) {
        return registrationConfiguration.isSetVnicProfileMappings() &&
                registrationConfiguration.getVnicProfileMappings().isSetRegistrationVnicProfileMappings();
    }


    private static Collection<ExternalVnicProfileMapping> mapVnicProfilesMapping(RegistrationVnicProfileMappings model) {
        return model.getRegistrationVnicProfileMappings()
                .stream()
                .map(regMapping ->
                        // when the 'from' details are not complete - ignore the mapping because it would
                        // be impossible to find a matching vnic for it anyway
                        isSetFrom(regMapping) ? createExternalVnicProfileMapping(regMapping) : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * <pre>
     * - Best effort conversion of the input user mapping REST API object into an internal representation.
     * - If there is no target 'to' then it is considered as 'no mapping specified' for this 'from'. therefore the
     *   user entry is ignored and the bll logic will try to use the sources on the ovf nic as the target.
     * </pre>
     * @param regMapping - user input for mapping
     * @return internal object representing the input user mapping
     */
    private static ExternalVnicProfileMapping createExternalVnicProfileMapping(RegistrationVnicProfileMapping regMapping) {
        ExternalVnicProfileMapping m = new ExternalVnicProfileMapping(
                regMapping.getFrom().getNetwork().getName(),
                regMapping.getFrom().getName());
        if(isSetToId(regMapping)) {
            try {
                Guid targetId = asGuid(regMapping.getTo().getId());
                m.setTargetProfileId(targetId);
            } catch (Exception e){
                //bad id - ignore
            }
        }
        if (isSetToName(regMapping)) {
            m.setTargetProfileName(regMapping.getTo().getName());
        }
        if (isSetToNetworkName(regMapping)) {
            m.setTargetNetworkName(regMapping.getTo().getNetwork().getName());
        }
        return m;
    }

    private static boolean isSetFrom(RegistrationVnicProfileMapping regMapping) {
        return regMapping.isSetFrom() && regMapping.getFrom().isSetNetwork() &&
                regMapping.getFrom().getNetwork().isSetName() && regMapping.getFrom().isSetName();
    }

    private static boolean isSetToId(RegistrationVnicProfileMapping regMapping) {
        return regMapping.isSetTo() && regMapping.getTo().isSetId();
    }

    private static boolean isSetToName(RegistrationVnicProfileMapping regMapping) {
        return regMapping.isSetTo() && regMapping.getTo().isSetName();
    }

    private static boolean isSetToNetworkName(RegistrationVnicProfileMapping regMapping) {
        return regMapping.isSetTo() && regMapping.getTo().isSetNetwork() &&
                regMapping.getTo().getNetwork().isSetName();
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
                        registrationMap -> registrationMap.isSetFrom() ? getLogicalUnitId(registrationMap.getFrom()) : null,
                        registrationMap -> registrationMap.isSetTo() ? DiskMapper.map(registrationMap.getTo(), null)
                                : null));
    }

    private static String getLogicalUnitId(Disk disk) {
        if (disk.getLunStorage() != null && disk.getLunStorage().getLogicalUnits() != null
                && disk.getLunStorage().getLogicalUnits().getLogicalUnits().size() > 0) {
            return disk.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).getId();
        }
        // In case no LUN id was found we return empty GUID to avoid null key initialization in the map.
        return Guid.Empty.toString();
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
