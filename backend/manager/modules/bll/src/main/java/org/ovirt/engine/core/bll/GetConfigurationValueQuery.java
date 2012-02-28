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
        Object returnValue = null;
        if (shouldReturnValue()) {
            try {
                GetConfigurationValueParameters params = getParameters();
                String version = params.getVersion();
                if (version == null) {
                    log.warnFormat("calling {0} with null version, using default {1} for version",
                            GetConfigurationValueQuery.class.getSimpleName(), Config.DefaultConfigurationVersion);
                    version = Config.DefaultConfigurationVersion;
                }
                ConfigValues value = ConfigValues.valueOf(params.getConfigValue().toString());
                returnValue = Config.<Object> GetValue(value, version);
            } catch (Exception e) {
                log.error("Unable to return config parameter: " + getParameters(), e);
            }
        }

        getQueryReturnValue().setReturnValue(returnValue);
    }

    /**
     * Validates if the query should return anything or not, according to the user's permissions:
     * <ul>
     * <li>If the query is run as an administrator (note that since we've reached the {@link #executeQueryCommand()} method,
     * we've already validated that the use is indeed an administrator), the results from the database queries should be returned.</li>
     * <li>If the query is run as a user, it may return results <b>ONLY</b> if the configuration value has {@link org.ovirt.engine.core.common.queries.ConfigurationValues.ConfigAuthType#User}.</li>
     * </ul>
     */
    private boolean shouldReturnValue() {
        return !getParameters().isFiltered() || !getParameters().getConfigValue().isAdmin();
    }
}
