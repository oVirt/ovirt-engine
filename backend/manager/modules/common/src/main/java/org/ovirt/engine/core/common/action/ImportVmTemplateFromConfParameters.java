package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmTemplateFromConfParameters extends ImportVmTemplateParameters implements Serializable, ImportParameters {
    private static final long serialVersionUID = -2440515728742118922L;

    private Map<String, String> clusterMap;
    private Map<String, Object> roleMap;
    private Map<String, String> domainMap;

    public ImportVmTemplateFromConfParameters(Guid storagePoolId,
            Guid sourceDomainId,
            Guid destDomainId,
            Guid clusterId,
            VmTemplate template) {
        super(storagePoolId, sourceDomainId, destDomainId, clusterId, template);
    }

    public Map<String, String> getClusterMap() {
        return clusterMap;
    }

    public void setClusterMap(Map<String, String> clusterMap) {
        this.clusterMap = clusterMap;
    }

    public Map<String, Object> getRoleMap() {
        return roleMap;
    }

    public void setRoleMap(Map<String, Object> roleMap) {
        this.roleMap = roleMap;
    }

    public Map<String, String> getDomainMap() {
        return domainMap;
    }

    public void setDomainMap(Map<String, String> domainMap) {
        this.domainMap = domainMap;
    }

    public Map<String, String> getAffinityGroupMap() {
        return null;
    }

    public void setAffinityGroupMap(Map<String, String> affinityGroupMap) {
    }

    public Map<String, String> getAffinityLabelMap() {
        return null;
    }

    public void setAffinityLabelMap(Map<String, String> affinityLabelMap) {
    }

    public Map<String, Object> getExternalLunMap() {
        return null;
    }

    public void setExternalLunMap(Map<String, Object> externalLunMap) {
    }

    public ImportVmTemplateFromConfParameters() {
        super();
    }
}
