package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.compat.KeyValuePairCompat;

public class GetConfigurationValuesQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    public GetConfigurationValuesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Map<KeyValuePairCompat<ConfigValues, String>, Object> configValuesMap = new HashMap<>();

        for (ConfigValues configValue : ConfigValues.values()) {
            // Ignore an admin configuration value on filtered mode
            if (!shouldReturnValue(configValue) || configValue == ConfigValues.Invalid) {
                continue;
            }

            Map<String, Object> values = Config.getValuesForAllVersions(configValue);
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                KeyValuePairCompat<ConfigValues, String> key = new KeyValuePairCompat<>(configValue, entry.getKey());
                configValuesMap.put(key, entry.getValue());
            }
        }

        getQueryReturnValue().setReturnValue(configValuesMap);
    }

    /**
     * Validates if the query should return anything or not, according to the user's permissions:
     * <ul>
     * <li>If the query is run as an administrator (note that since we've reached the {@link #executeQueryCommand()} method,
     * we've already validated that the use is indeed an administrator), the results from the database queries should be returned.</li>
     * <li>If the query is run as a user, it may return results <b>ONLY</b> if the configuration value has {@link ConfigValues.ClientAccessLevel#User}.</li>
     * </ul>
     */
    private boolean shouldReturnValue(ConfigValues configValue) {
        return !getParameters().isFiltered() || configValue.nonAdminVisible();
    }
}
