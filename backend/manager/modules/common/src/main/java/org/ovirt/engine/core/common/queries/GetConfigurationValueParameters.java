package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class GetConfigurationValueParameters extends QueryParametersBase {
    private static final long serialVersionUID = -2166600418595305124L;

    private ConfigValues configValue;
    private String version;

    public GetConfigurationValueParameters() {
        this(null);
    }

    public GetConfigurationValueParameters(ConfigValues cVal) {
        this(cVal, null);
    }

    public ConfigValues getConfigValue() {
        return configValue;
    }

    public void setConfigValue(ConfigValues configValue) {
        this.configValue = configValue;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public GetConfigurationValueParameters(ConfigValues configValue, String version) {
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
