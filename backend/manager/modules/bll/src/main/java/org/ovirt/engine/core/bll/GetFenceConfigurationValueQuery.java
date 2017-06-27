package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.utils.pm.FenceConfigHelper;

public class GetFenceConfigurationValueQuery<P extends GetConfigurationValueParameters> extends QueriesCommandBase<P> {
    public GetFenceConfigurationValueQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        if (shouldReturnValue()) {
            try {
                final GetConfigurationValueParameters params = getParameters();
                final ConfigValues value = params.getConfigValue();
                String version = params.getVersion();
                if (version == null) {
                    log.warn("calling {} ({}) with null version, using default {} for version",
                            GetConfigurationValueQuery.class.getSimpleName(), ConfigCommon.defaultConfigurationVersion, value);
                    version = ConfigCommon.defaultConfigurationVersion;
                }
                getQueryReturnValue().setReturnValue(FenceConfigHelper.getFenceConfigurationValue(getParameters().getConfigValue().toString(), version));
            } catch (Exception e) {
                log.error("Unable to return config parameter {}: {}", getParameters(), e.getMessage() );
                log.debug("Exception", e);
            }
        }
    }

    /**
     * Validates if the query should return anything or not, according to the user's permissions:
     * <ul>
     * <li>If the query is run as an administrator (note that since we've reached the {@link #executeQueryCommand()} method,
     * we've already validated that the use is indeed an administrator), the results from the database queries should be returned.</li>
     * <li>If the query is run as a user, it may return results <b>ONLY</b> if the configuration value has {@link ConfigValues.ClientAccessLevel#User}.</li>
     * </ul>
     */
    private boolean shouldReturnValue() {
        return !getParameters().isFiltered() || getParameters().getConfigValue().nonAdminVisible();
    }
}
