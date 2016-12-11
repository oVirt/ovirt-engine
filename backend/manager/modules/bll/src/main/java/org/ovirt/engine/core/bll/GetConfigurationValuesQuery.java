package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.Version;

public class GetConfigurationValuesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    private static final List<String> versions = getVersionsList();

    public GetConfigurationValuesQuery(P parameters) {
        super(parameters);
    }

    private static List<String> getVersionsList() {
        return Stream.concat(Stream.of(ConfigCommon.defaultConfigurationVersion),
                Version.ALL.stream().map(Object::toString))
                .collect(Collectors.toList());
    }

    @Override
    protected void executeQueryCommand() {
        Map<KeyValuePairCompat<ConfigurationValues, String>, Object> configValuesMap = new HashMap<>();

        for (ConfigurationValues configValue : ConfigurationValues.values()) {
            // Ignore an admin configuration value on filtered mode
            // Ignore a configuration value that doesn't exist in ConfigValues enum
            if (!shouldReturnValue(configValue)) {
                continue;
            }

            // Adding a configuration value for each version
            for (String version : versions) {
                populateValueForConfigValue(configValue, version, configValuesMap);
            }
        }

        getQueryReturnValue().setReturnValue(configValuesMap);
    }

    private void populateValueForConfigValue(ConfigurationValues configValue,
            String version,
            Map<KeyValuePairCompat<ConfigurationValues, String>, Object> configValuesMap) {
        KeyValuePairCompat<ConfigurationValues, String> key = new KeyValuePairCompat<>(configValue, version);
        Object value = Config.getValue(ConfigValues.valueOf(configValue.toString()), version);

        configValuesMap.put(key, value);
    }

    /**
     * Validates if the query should return anything or not, according to the user's permissions:
     * <ul>
     * <li>If the query is run as an administrator (note that since we've reached the {@link #executeQueryCommand()} method,
     * we've already validated that the use is indeed an administrator), the results from the database queries should be returned.</li>
     * <li>If the query is run as a user, it may return results <b>ONLY</b> if the configuration value has {@link org.ovirt.engine.core.common.queries.ConfigurationValues.ConfigAuthType#User}.</li>
     * </ul>
     */
    private boolean shouldReturnValue(ConfigurationValues configValue) {
        return !getParameters().isFiltered() || !configValue.isAdmin();
    }
}
