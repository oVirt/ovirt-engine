package org.ovirt.engine.core.common.businessentities;

import java.util.Map;

public class ExternalHostGroup implements ExternalEntityBase {
    private static final long serialVersionUID = -3099054972843803212L;

    private String name;

    private int subnetId;
    private int domainId;
    private int environmentId;
    private String environmentName;
    private int hostgroupId;
    private int operatingsystemId;
    private int ptableId;
    private int mediumId;
    private int architectureId;
    private int puppetProxyId;
    private int puppetCaProxyId;
    private Map<String, String> parameters;
    private String ancestry;
    private String subnetName;
    private String operatingsystemName;
    private String domainName;
    private String architectureName;
    private String ptableName;
    private String mediumName;

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getAncestry() {
        return ancestry;
    }

    public void setAncestry(String ancestry) {
        this.ancestry = ancestry;
    }

    public String getArchitectureName() {
        return architectureName;
    }

    public void setArchitectureName(String architectureName) {
        this.architectureName = architectureName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getOperatingsystemName() {
        return operatingsystemName;
    }

    public void setOperatingsystemName(String operatingsystemName) {
        this.operatingsystemName = operatingsystemName;
    }

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public int getHostgroupId() {
        return hostgroupId;
    }

    public void setHostgroupId(int id) {
        this.hostgroupId = id;
    }

    public int getOperatingsystemId() {
        return operatingsystemId;
    }

    public void setOperatingsystemId(int os_id) {
        this.operatingsystemId = os_id;
    }

    public int getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(int subnetId) {
        this.subnetId = subnetId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public int getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(int environmentId) {
        this.environmentId = environmentId;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public int getPtableId() {
        return ptableId;
    }

    public void setPtableId(int ptableId) {
        this.ptableId = ptableId;
    }

    public int getMediumId() {
        return mediumId;
    }

    public void setMediumId(int mediumId) {
        this.mediumId = mediumId;
    }

    public int getArchitectureId() {
        return architectureId;
    }

    public void setArchitectureId(int architectureId) {
        this.architectureId = architectureId;
    }

    @Override
    public String getDescription() {
        return "OS: " + (operatingsystemName != null ? operatingsystemName : "[N/A]") + "\n" +
                " | Subnet: " + (subnetName != null ? subnetName : "[N/A]") + "\n" +
                " | Domain: " + (domainName != null ? domainName : "[N/A]") + "\n" +
                " | Arch: " + (architectureName != null ? architectureName : "[N/A]");
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPtableName() {
        return ptableName;
    }

    public void setPtableName(String ptableName) {
        this.ptableName = ptableName;
    }

    public String getMediumName() {
        return mediumName;
    }

    public void setMediumName(String mediumName) {
        this.mediumName = mediumName;
    }

    public void setPuppetProxyId(int puppetProxyId) {
        this.puppetProxyId = puppetProxyId;
    }

    public void setPuppetCaProxyId(int puppetCaProxyId) {
        this.puppetCaProxyId = puppetCaProxyId;
    }

    public int getPuppetProxyId() {
        return puppetProxyId;
    }

    public int getPuppetCaProxyId() {
        return puppetCaProxyId;
    }
}
