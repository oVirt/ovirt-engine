package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetSystemOptionParameters;

public class GetSystemOptionQuery<P extends GetSystemOptionParameters> extends QueriesCommandBase<P> {

    GetSystemOptionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Map<String, Object> result;

        if (!shouldReturnValue()) {
            return;
        }

        String version = getParameters().getOptionVersion();
        if (version == null) {
            result = Config.getValuesForAllVersions(getParameters().getOptionName());
        } else {
            if (!version.equals(ConfigCommon.defaultConfigurationVersion)
                && !Config.valueExists(getParameters().getOptionName(), version)) {
                return;
            }

            result = new HashMap<>();
            result.put(
                version,
                Config.getValue(getParameters().getOptionName(), version)
            );
        }
        getQueryReturnValue().setReturnValue(result);
    }

    /**
     * Validates if the query should return anything or not, according to the user's permissions:
     * <ul>
     * <li>If the query is run by administrator, the results are return if configuration value has not
     * {@link ConfigValues.ClientAccessLevel#Internal}.</li>
     * <li>If the query is run by user, it may return results <b>ONLY</b> if the configuration value has
     * {@link ConfigValues.ClientAccessLevel#User}.</li>
     * </ul>
     */
    private boolean shouldReturnValue() {
        DbUser user = getUser();
        ConfigValues config = getParameters().getOptionName();
        return user.isAdmin()
            ? config.getAccessLevel() != ConfigValues.ClientAccessLevel.Internal
            : config.nonAdminVisible();
    }
}
