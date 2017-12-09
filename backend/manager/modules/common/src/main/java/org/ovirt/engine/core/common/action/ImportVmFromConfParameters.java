package org.ovirt.engine.core.common.action;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmFromConfParameters extends ImportVmParameters implements ImportFromConfParameters {
    private static final long serialVersionUID = 757253818528428256L;

    private Map<String, String> clusterMap;
    private Map<String, String> roleMap;
    private Map<String, String> domainMap;
    private Map<String, String> affinityGroupMap;
    private Map<String, String> affinityLabelMap;
    private Map<String, Object> externalLunMap;

    private List<AffinityGroup> affinityGroups;
    private Set<DbUser> dbUsers;
    private Map<String, Set<String>> userToRoles  = new HashMap<>();
    private List<Label> affinityLabels;
    private Collection<ExternalVnicProfileMapping> externalVnicProfileMappings;

    public ImportVmFromConfParameters() {
        this(Collections.emptyList(), false);
    }

    public ImportVmFromConfParameters(
            Collection<ExternalVnicProfileMapping> externalVnicProfileMappings,
            boolean reassignBadMacs) {
        super(reassignBadMacs);
        this.externalVnicProfileMappings = Objects.requireNonNull(externalVnicProfileMappings);
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
    public Map<String, String> getRoleMap() {
        return roleMap;
    }

    @Override
    public void setRoleMap(Map<String, String> roleMap) {
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

    public List<Label> getAffinityLabels() {
        return affinityLabels;
    }

    public void setAffinityLabels(List<Label> affinityLabels) {
        this.affinityLabels = affinityLabels;
    }

    public Set<DbUser> getDbUsers() {
        return dbUsers;
    }

    public void setDbUsers(Set<DbUser> dbUsers) {
        this.dbUsers = dbUsers;
    }

    public Map<String, Set<String>> getUserToRoles() {
        return userToRoles;
    }

    public void setUserToRoles(Map<String, Set<String>> userToRoles) {
        this.userToRoles = userToRoles;
    }

    @Override
    public Collection<ExternalVnicProfileMapping> getExternalVnicProfileMappings() {
        return externalVnicProfileMappings;
    }

    @Override
    public void setExternalVnicProfileMappings(Collection<ExternalVnicProfileMapping> externalVnicProfileMappings) {
        this.externalVnicProfileMappings = externalVnicProfileMappings;
    }
}
