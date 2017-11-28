package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

public class ForemanHostGroup implements Serializable {
    private static final long serialVersionUID = -3099054972843803212L;

    private String name;
    private int id;
    @JsonProperty("subnet_id")
    private int subnetId;
    @JsonProperty("operatingsystem_id")
    private int operatingSystemId;
    @JsonProperty("domain_id")
    private int domainId;
    @JsonProperty("environment_id")
    private int environmentId;
    @JsonProperty("ptable_id")
    private int ptableId;
    @JsonProperty("medium_id")
    private int mediumId;
    @JsonProperty("architecture_id")
    private int architectureId;
    @JsonProperty("puppet_proxy_id")
    private int puppetProxyId;
    @JsonProperty("puppet_ca_proxy_id")
    private int puppetCaProxyId;
    @JsonProperty("puppet_class_ids")
    private int[] puppetClassIds;
    private Map<String, String> parameters;
    private String ancestry;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(int subnetId) {
        this.subnetId = subnetId;
    }

    public int getOperatingSystemId() {
        return operatingSystemId;
    }

    public void setOperatingSystemId(int operatingSystemId) {
        this.operatingSystemId = operatingSystemId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
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

    public int getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(int environmentId) {
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
