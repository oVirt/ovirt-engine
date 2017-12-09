package org.ovirt.engine.core.common.action;

import java.util.Collection;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;

public interface ImportFromConfParameters {

    Map<String, String> getClusterMap();

    void setClusterMap(Map<String, String> clusterMap);

    Map<String, String> getRoleMap();

    void setRoleMap(Map<String, String> roleMap);

    Map<String, String> getDomainMap();

    void setDomainMap(Map<String, String> domainMap);

    Map<String, String> getAffinityGroupMap();

    void setAffinityGroupMap(Map<String, String> affinityGroupMap);

    Map<String, String> getAffinityLabelMap();

    void setAffinityLabelMap(Map<String, String> affinityLabelMap);

    Map<String, Object> getExternalLunMap();

    void setExternalLunMap(Map<String, Object> externalLunMap);

    Collection<ExternalVnicProfileMapping> getExternalVnicProfileMappings();

    void setExternalVnicProfileMappings(Collection<ExternalVnicProfileMapping> externalVnicProfilesMap);
}
