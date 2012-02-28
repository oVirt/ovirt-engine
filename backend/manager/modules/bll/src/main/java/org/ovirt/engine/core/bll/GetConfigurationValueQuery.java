package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;

public class GetConfigurationValueQuery<P extends GetConfigurationValueParameters> extends QueriesCommandBase<P> {
    public GetConfigurationValueQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        try {
            GetConfigurationValueParameters params = getParameters();
            String version = params.getVersion();
            if (version == null) {
                log.warnFormat("calling {0} with null version, using default {1} for version",
                        GetConfigurationValueQuery.class.getSimpleName(), Config.DefaultConfigurationVersion);
                version = Config.DefaultConfigurationVersion;
            }
            ConfigValues value = ConfigValues.valueOf(params.getConfigValue().toString());
            getQueryReturnValue().setReturnValue(Config.<Object> GetValue(value, version));
        } catch (Exception e) {
            log.error("Unable to return config parameter: " + getParameters(), e);
            getQueryReturnValue().setReturnValue(null);
        }
    }
}
