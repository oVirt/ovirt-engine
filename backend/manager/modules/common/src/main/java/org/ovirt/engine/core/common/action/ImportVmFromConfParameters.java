package org.ovirt.engine.core.common.action;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmFromConfParameters extends ImportVmParameters implements ImportFromConfParameters {
    private static final long serialVersionUID = 757253818528428256L;

    private Map<String, String> clusterMap;
    private Map<String, Object> roleMap;
    private Map<String, String> domainMap;
    private Map<String, String> affinityGroupMap;
    private Map<String, String> affinityLabelMap;
    private Map<String, Object> externalLunMap;

    private List<AffinityGroup> affinityGroups;

    public ImportVmFromConfParameters() {
        this(Collections.emptyList(), false);
    }

    public ImportVmFromConfParameters(
            Collection<ExternalVnicProfileMapping> externalVnicProfileMappings,
            boolean reassignBadMacs) {
        super(externalVnicProfileMappings, reassignBadMacs);
    }

    public ImportVmFromConfParameters(VM vm,
            Guid sourceStorageDomainId,
            Guid destStorageDomainId,
            Guid storagePoolId,
            Guid clusterId) {
        super(vm, sourceStorageDomainId, destStorageDomainId, storagePoolId, clusterId);
    }

    @Override
    public Map<String, String> getClusterMap() {
        return clusterMap;
    }

    @Override
    public void setClusterMap(Map<String, String> clusterMap) {
        this.clusterMap = clusterMap;
    }

    @Override
    public Map<String, Object> getRoleMap() {
        return roleMap;
    }

    @Override
    public void setRoleMap(Map<String, Object> roleMap) {
        this.roleMap = roleMap;
    }

    @Override
    public Map<String, String> getDomainMap() {
        return domainMap;
    }

    @Override
    public void setDomainMap(Map<String, String> domainMap) {
        this.domainMap = domainMap;
    }

    @Override
    public Map<String, String> getAffinityGroupMap() {
        return affinityGroupMap;
    }

    @Override
    public void setAffinityGroupMap(Map<String, String> affinityGroupMap) {
        this.affinityGroupMap = affinityGroupMap;
    }

    @Override
    public Map<String, String> getAffinityLabelMap() {
        return affinityLabelMap;
    }

    @Override
    public void setAffinityLabelMap(Map<String, String> affinityLabelMap) {
        this.affinityLabelMap = affinityLabelMap;
    }

    @Override
    public Map<String, Object> getExternalLunMap() {
        return externalLunMap;
    }

    @Override
    public void setExternalLunMap(Map<String, Object> externalLunMap) {
        this.externalLunMap = externalLunMap;
    }

    public List<AffinityGroup> getAffinityGroups() {
        return affinityGroups;
    }

    public void setAffinityGroups(List<AffinityGroup> affinityGroups) {
        this.affinityGroups = affinityGroups;
    }
}
