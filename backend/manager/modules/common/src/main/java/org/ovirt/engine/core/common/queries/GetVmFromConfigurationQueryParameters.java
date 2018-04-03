package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.ConfigurationType;

public class GetVmFromConfigurationQueryParameters extends QueryParametersBase {

    private String vmConfiguration;
    private ConfigurationType configurationType;

    public GetVmFromConfigurationQueryParameters() {
    }

    public GetVmFromConfigurationQueryParameters(ConfigurationType configurationType, String vmConfiguration) {
        this.configurationType = configurationType;
        this.vmConfiguration = vmConfiguration;
    }

    public ConfigurationType getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(ConfigurationType configurationType) {
        this.configurationType = configurationType;
    }

    public String getVmConfiguration() {
        return vmConfiguration;
    }
    public void setVmConfiguration(String vmConfiguration) {
        this.vmConfiguration = vmConfiguration;
    }
}
