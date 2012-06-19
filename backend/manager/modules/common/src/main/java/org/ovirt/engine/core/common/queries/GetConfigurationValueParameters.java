package org.ovirt.engine.core.common.queries;


public class GetConfigurationValueParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5889171970595969719L;

    public GetConfigurationValueParameters(ConfigurationValues cVal) {
        _configValue = cVal;
    }

    private ConfigurationValues _configValue = ConfigurationValues.forValue(0);

    public ConfigurationValues getConfigValue() {
        return _configValue;
    }

    private String privateVersion;

    public String getVersion() {
        return privateVersion;
    }

    public void setVersion(String value) {
        privateVersion = value;
    }

    public GetConfigurationValueParameters(ConfigurationValues cVal, String version) {
        _configValue = cVal;
        privateVersion = version;
    }

    public GetConfigurationValueParameters() {
    }
}
