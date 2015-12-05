package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;
import java.util.Map;

public class ForemanHostGroup implements Serializable {
    private static final long serialVersionUID = -3099054972843803212L;

    private String name;
    private int id;
    private int subnetId;
    private int operatingSystemId;
    private int domainId;
    private int environmentId;
    private int ptableId;
    private int mediumId;
    private int architectureId;
    private int[] puppetClassIds;
    private Map<String, String> parameters;
    private String ancestry;
    private String subnetName;
    private String operatingSystemName;
    private String domainName;
    private String architectureName;
    private String environmentName;
    private String ptableName;
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
}
