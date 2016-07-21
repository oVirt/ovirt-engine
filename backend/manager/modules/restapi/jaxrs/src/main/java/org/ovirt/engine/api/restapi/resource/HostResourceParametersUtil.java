package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.util.ParametersHelper.getBooleanParameter;

import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;

public class HostResourceParametersUtil {
    public static final String QUERY_PARAM_DEPLOY_HOSTED_ENGINE = "deploy_hosted_engine";
    public static final String QUERY_PARAM_UNDEPLOY_HOSTED_ENGINE = "undeploy_hosted_engine";

    public static HostedEngineDeployConfiguration getHostedEngineDeployConfiguration(BaseBackendResource resource) {
        // deploy?
        if (getBooleanParameter(resource.getHttpHeaders(),
                resource.getUriInfo(),
                QUERY_PARAM_DEPLOY_HOSTED_ENGINE,
                true,
                false)) {
            return new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.DEPLOY);
        }
        // undeploy?
        if (getBooleanParameter(resource.getHttpHeaders(),
                resource.getUriInfo(),
                QUERY_PARAM_UNDEPLOY_HOSTED_ENGINE,
                true,
                false)) {
            return new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.UNDEPLOY);
        }
        // null will be safely ignored and is backward compatible
        return null;
    }
}
