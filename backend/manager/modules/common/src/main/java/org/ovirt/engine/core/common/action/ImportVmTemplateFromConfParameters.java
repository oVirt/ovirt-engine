package org.ovirt.engine.core.common.action;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmTemplateFromConfParameters extends ImportVmTemplateParameters implements ImportFromConfParameters {
    private static final long serialVersionUID = -2440515728742118922L;

    private Map<String, String> clusterMap;
    private Map<String, String> roleMap;
    private Map<String, String> domainMap;

    private Set<DbUser> dbUsers;
    private Map<String, Set<String>> userToRoles  = new HashMap<>();
    private Collection<ExternalVnicProfileMapping> externalVnicProfileMappings;

    public ImportVmTemplateFromConfParameters() {
        super();
        this.externalVnicProfileMappings = Collections.emptyList();
    }

    public ImportVmTemplateFromConfParameters(Guid storagePoolId,
            Guid sourceDomainId,
            Guid destDomainId,
            Guid clusterId,
            VmTemplate template) {
        super(storagePoolId, sourceDomainId, destDomainId, clusterId, template);
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
        return null;
    }

    @Override
    public void setAffinityGroupMap(Map<String, String> affinityGroupMap) {
    }

    @Override
    public Map<String, String> getAffinityLabelMap() {
        return null;
    }

    @Override
    public void setAffinityLabelMap(Map<String, String> affinityLabelMap) {
    }

    @Override
    public Map<String, Object> getExternalLunMap() {
        return null;
    }

    @Override
    public void setExternalLunMap(Map<String, Object> externalLunMap) {
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
        this.externalVnicProfileMappings = Objects.requireNonNull(externalVnicProfileMappings);
    }
}
