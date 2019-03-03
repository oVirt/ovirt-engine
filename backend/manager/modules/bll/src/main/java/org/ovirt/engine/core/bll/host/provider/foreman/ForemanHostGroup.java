package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

public class ForemanHostGroup implements Serializable {
    private static final long serialVersionUID = -3099054972843803212L;

    private String name;
    private int id;
    @JsonProperty("subnet_id")
    private Integer subnetId;
    @JsonProperty("operatingsystem_id")
    private Integer operatingSystemId;
    @JsonProperty("domain_id")
    private Integer domainId;
    @JsonProperty("environment_id")
    private Integer environmentId;
    @JsonProperty("ptable_id")
    private Integer ptableId;
    @JsonProperty("medium_id")
    private Integer mediumId;
    @JsonProperty("architecture_id")
    private Integer architectureId;
    @JsonProperty("puppet_proxy_id")
    private Integer puppetProxyId;
    @JsonProperty("puppet_ca_proxy_id")
    private Integer puppetCaProxyId;
    @JsonProperty("puppet_class_ids")
    private int[] puppetClassIds;
    private Map<String, String> parameters;
    private String ancestry;
    private String title;
    @JsonProperty("subnet_name")
    private String subnetName;
    @JsonProperty("operatingsystem_name")
    private String operatingSystemName;
    @JsonProperty("domain_name")
    private String domainName;
    @JsonProperty("architecture_name")
    private String architectureName;
    @JsonProperty("environment_name")
    private String environmentName;
    @JsonProperty("ptable_name")
    private String ptableName;
    @JsonProperty("medium_name")
    private String mediumName;

    public String getAncestry() {
        return ancestry;
    }

    public void setAncestry(String ancestry) {
        this.ancestry = ancestry;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title= title;
    }

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public String getOperatingSystemName() {
        return operatingSystemName;
    }

    public void setOperatingSystemName(String operatingSystemName) {
        this.operatingSystemName = operatingSystemName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getArchitectureName() {
        return architectureName;
    }

    public void setArchitectureName(String architectureName) {
        this.architectureName = architectureName;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(Integer subnetId) {
        this.subnetId = subnetId;
    }

    public Integer getOperatingSystemId() {
        return operatingSystemId;
    }

    public void setOperatingSystemId(Integer operatingSystemId) {
        this.operatingSystemId = operatingSystemId;
    }

    public Integer getDomainId() {
        return domainId;
    }

    public void setDomainId(Integer domainId) {
        this.domainId = domainId;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public int[] getPuppetClassIds() {
        return puppetClassIds;
    }

    public void setPuppetClassIds(int[] puppetClassIds) {
        this.puppetClassIds = puppetClassIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Integer environmentId) {
        this.environmentId = environmentId;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
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
}
