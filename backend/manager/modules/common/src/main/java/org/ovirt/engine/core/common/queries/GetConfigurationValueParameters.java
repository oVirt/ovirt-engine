package org.ovirt.engine.core.common.queries;

public class GetConfigurationValueParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5889171970595969719L;

    public GetConfigurationValueParameters(ConfigurationValues cVal) {
        this(cVal, null);
    }

    private ConfigurationValues _configValue;

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
        setRefresh(false);
    }

    public GetConfigurationValueParameters() {
        this(ConfigurationValues.MaxNumOfVmCpus);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(50);
        builder.append("version: "); //$NON-NLS-1$
        builder.append(getVersion());
        builder.append(", configuration value: ");
        builder.append(getConfigValue());
        builder.append(", ");
        builder.append(super.toString());
        return builder.toString();
    }
}
