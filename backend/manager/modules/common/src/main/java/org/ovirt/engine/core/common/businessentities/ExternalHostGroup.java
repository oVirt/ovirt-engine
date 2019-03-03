package org.ovirt.engine.core.common.businessentities;

import java.util.Map;

public class ExternalHostGroup implements ExternalEntityBase {
    private static final long serialVersionUID = -3099054972843803212L;

    private String name;

    private String title;
    private Integer subnetId;
    private Integer domainId;
    private Integer environmentId;
    private String environmentName;
    private Integer hostgroupId;
    private Integer operatingsystemId;
    private Integer ptableId;
    private Integer mediumId;
    private Integer architectureId;
    private Integer puppetProxyId;
    private Integer puppetCaProxyId;
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

    public Integer getHostgroupId() {
        return hostgroupId;
    }

    public void setHostgroupId(Integer id) {
        this.hostgroupId = id;
    }

    public Integer getOperatingsystemId() {
        return operatingsystemId;
    }

    public void setOperatingsystemId(Integer osId) {
        this.operatingsystemId = osId;
    }

    public Integer getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(Integer subnetId) {
        this.subnetId = subnetId;
    }

    public Integer getDomainId() {
        return domainId;
    }

    public void setDomainId(Integer domainId) {
        this.domainId = domainId;
    }

    public Integer getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Integer environmentId) {
        this.environmentId = environmentId;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Integer getPtableId() {
        return ptableId;
    }

    public void setPtableId(Integer ptableId) {
        this.ptableId = ptableId;
    }

    public Integer getMediumId() {
        return mediumId;
    }

    public void setMediumId(Integer mediumId) {
        this.mediumId = mediumId;
    }

    public Integer getArchitectureId() {
        return architectureId;
    }

    public void setArchitectureId(Integer architectureId) {
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

    @Override
    public String getViewableName() {
        return getTitle();
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

    public void setPuppetProxyId(Integer puppetProxyId) {
        this.puppetProxyId = puppetProxyId;
    }

    public void setPuppetCaProxyId(Integer puppetCaProxyId) {
        this.puppetCaProxyId = puppetCaProxyId;
    }

    public Integer getPuppetProxyId() {
        return puppetProxyId;
    }

    public Integer getPuppetCaProxyId() {
        return puppetCaProxyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
