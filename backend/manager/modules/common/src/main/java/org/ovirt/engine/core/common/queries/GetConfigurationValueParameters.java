package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class GetConfigurationValueParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5889171970595969719L;

    private ConfigurationValues configValue;
    private String version;

    public GetConfigurationValueParameters() {
        this(null);
    }

    public GetConfigurationValueParameters(ConfigurationValues cVal) {
        this(cVal, null);
    }

    public ConfigurationValues getConfigValue() {
        return configValue;
    }

    public void setConfigValue(ConfigurationValues configValue) {
        this.configValue = configValue;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public GetConfigurationValueParameters(ConfigurationValues configValue, String version) {
        this.configValue = configValue;
        this.version = version;
        setRefresh(false);
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("version", getVersion())
                .append("configurationValue", getConfigValue());
    }
}
