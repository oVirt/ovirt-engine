package org.ovirt.engine.core.bll.validator;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.pm.FenceProxyLocator;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.utils.pm.FenceConfigHelper;

@Singleton
public class FenceValidator {
    @Inject
    private BackendInternal backend;
    @Inject
    private FenceAgentDao fenceAgentDao;

    public boolean isProxyHostAvailable(VDS vds, List<String> messages) {
        FenceProxyLocator proxyHostLocator = getProxyLocator(vds);
        if (!proxyHostLocator.isProxyHostAvailable()) {
            messages.add(EngineMessage.VDS_NO_VDS_PROXY_FOUND.name());
            return false;
        } else {
            return true;
        }
    }

    public boolean isStartupTimeoutPassed() {
        // check if we are in the interval of X seconds from startup
        // if yes , system is still initializing , ignore fence operations
        Date waitTo = backend.getStartedAt().addSeconds(Config.getValue(ConfigValues.DisableFenceAtStartupInSec));
        Date now = new Date();
        return waitTo.before(now) || waitTo.equals(now);
    }

    public boolean isStartupTimeoutPassed(List<String> messages) {
        boolean startupTimeoutPassed = isStartupTimeoutPassed();
        if (!startupTimeoutPassed) {
            messages.add(EngineMessage.VDS_FENCE_DISABLED_AT_SYSTEM_STARTUP_INTERVAL.name());
        }
        return startupTimeoutPassed;
    }

    public boolean isPowerManagementEnabledAndLegal(VDS vds, Cluster cluster, List<String> messages) {
        if (!(vds.isPmEnabled()
                && isPowerManagementLegal(fenceAgentDao.getFenceAgentsForHost(vds.getId()), cluster, messages))) {
            messages.add(EngineMessage.VDS_FENCE_DISABLED.name());
            return false;
        } else {
            return true;
        }
    }

    public boolean isHostExists(VDS vds, List<String> messages) {
        if (vds == null) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST.name());
            return false;
        } else {
            return true;
        }
    }

    private boolean isPowerManagementLegal(List<FenceAgent> fenceAgents, Cluster cluster, List<String> messages) {
        if (fenceAgents == null || fenceAgents.isEmpty()) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_PM_ENABLED_WITHOUT_AGENT.name());
            return false;
        } else {
            return isCompatibleAgentExists(fenceAgents, cluster, messages);
        }
    }

    private boolean isCompatibleAgentExists(List<FenceAgent> fenceAgents, Cluster cluster, List<String> messages) {
        for (FenceAgent agent : fenceAgents) {
            if (isFenceAgentVersionCompatible(agent, cluster.getCompatibilityVersion().toString(), messages)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if given agent is supported by cluster version
     */
    public boolean isFenceAgentVersionCompatible(FenceAgent agent,
            String clusterCompatibilityVersion,
            List<String> messages) {
        if (!Pattern.compile(String.format("(,|^)%1$s(,|$)", agent.getType())).matcher(
                FenceConfigHelper.getFenceConfigurationValue(ConfigValues.VdsFenceType.name(),
                        clusterCompatibilityVersion)
                ).find()) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_AGENT_NOT_SUPPORTED.name());
            return false;
        } else {
            return true;
        }
    }

    protected FenceProxyLocator getProxyLocator(VDS vds) {
        return new FenceProxyLocator(vds);
    }
}
