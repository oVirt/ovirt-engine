package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;
import java.util.Map;

public class ForemanHostGroup implements Serializable {
    private static final long serialVersionUID = -3099054972843803212L;

    private String name;
    private int id;
    private int subnet_id;
    private int operatingsystem_id;
    private int domain_id;
    private int environment_id;
    private int ptable_id;
    private int medium_id;
    private int architecture_id;
    private int[] puppetclass_ids;
    private Map<String, String> parameters;
    private String subnet_name;
    private String operatingsystem_name;
    private String domain_name;
    private String architecture_name;

    public String getSubnet_name() { return subnet_name; }
    public void setSubnet_name(String subnet_name) { this.subnet_name = subnet_name; }
    public String getOperatingsystem_name() { return operatingsystem_name; }
    public void setOperatingsystem_name(String operatingsystem_name) { this.operatingsystem_name = operatingsystem_name; }
    public String getDomain_name() { return domain_name; }
    public void setDomain_name(String domain_name) { this.domain_name = domain_name; }
    public String getArchitecture_name() { return architecture_name; }
    public void setArchitecture_name(String architecture_name) { this.architecture_name = architecture_name; }
    public int getPtable_id() {
        return ptable_id;
    }
    public void setPtable_id(int ptable_id) {
        this.ptable_id = ptable_id;
    }
    public int getMedium_id() {
        return medium_id;
    }
    public void setMedium_id(int medium_id) {
        this.medium_id = medium_id;
    }
    public int getArchitecture_id() { return architecture_id; }
    public void setArchitecture_id(int architecture_id) {
        this.architecture_id = architecture_id;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getSubnet_id() {
        return subnet_id;
    }
    public void setSubnet_id(int subnet_id) {
        this.subnet_id = subnet_id;
    }
    public int getOperatingsystem_id() {
        return operatingsystem_id;
    }
    public void setOperatingsystem_id(int operatingsystem_id) {
        this.operatingsystem_id = operatingsystem_id;
    }
    public int getDomain_id() {
        return domain_id;
    }
    public void setDomain_id(int domain_id) {
        this.domain_id = domain_id;
    }
    public Map<String, String> getParameters() {
        return parameters;
    }
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    public int[] getPuppetclass_ids() {
        return puppetclass_ids;
    }
    public void setPuppetclass_ids(int[] puppetclass_ids) {
        this.puppetclass_ids = puppetclass_ids;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getEnvironment_id() {
        return environment_id;
    }
    public void setEnvironment_id(int environment_id) {
        this.environment_id = environment_id;
    }
}
