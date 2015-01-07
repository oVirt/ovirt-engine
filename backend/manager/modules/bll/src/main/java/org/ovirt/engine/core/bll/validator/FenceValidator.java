package org.ovirt.engine.core.bll.validator;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.FenceProxyLocator;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.utils.pm.FenceConfigHelper;

public class FenceValidator {

    public boolean isProxyHostAvailable(VDS vds, List<String> messages) {
        FenceProxyLocator proxyHostLocator = new FenceProxyLocator(vds);
        if (!proxyHostLocator.isProxyHostAvailable()) {
            messages.add(VdcBllMessages.VDS_NO_VDS_PROXY_FOUND.name());
            return false;
        } else {
            return true;
        }
    }

    public boolean isStartupTimeoutPassed(List<String> messages) {
        // check if we are in the interval of X seconds from startup
        // if yes , system is still initializing , ignore fence operations
        Date waitTo =
                Backend.getInstance()
                        .getStartedAt()
                        .addSeconds((Integer) Config.getValue(ConfigValues.DisableFenceAtStartupInSec));
        Date now = new Date();
        if (!(waitTo.before(now) || waitTo.equals(now))) {
            messages.add(VdcBllMessages.VDS_FENCE_DISABLED_AT_SYSTEM_STARTUP_INTERVAL.name());
            return false;
        } else {
            return true;
        }
    }

    public boolean isPowerManagementEnabledAndLegal(VDS vds, VDSGroup vdsGroup, List<String> messages) {
        if (!(vds.isPmEnabled() && isPowerManagementLegal(vds.getFenceAgents(), vdsGroup, messages))) {
            messages.add(VdcBllMessages.VDS_FENCE_DISABLED.name());
            return false;
        } else {
            return true;
        }
    }

    public boolean isHostExists(VDS vds, List<String> messages) {
        if (vds == null) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST.name());
            return false;
        } else {
            return true;
        }
    }

    private boolean isPowerManagementLegal(List<FenceAgent> fenceAgents, VDSGroup vdsGroup, List<String> messages) {
        if (fenceAgents == null || fenceAgents.isEmpty()) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_PM_ENABLED_WITHOUT_AGENT.name());
            return false;
        } else {
            return isCompatibleAgentExists(fenceAgents, vdsGroup, messages);
        }
    }

    private boolean isCompatibleAgentExists(List<FenceAgent> fenceAgents, VDSGroup vdsGroup, List<String> messages) {
        for (FenceAgent agent : fenceAgents) {
            if (isFenceAgentVersionCompatible(agent, vdsGroup.getcompatibility_version().toString(), messages)) {
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
        if (!Regex.IsMatch(FenceConfigHelper.getFenceConfigurationValue(ConfigValues.VdsFenceType.name(),
                clusterCompatibilityVersion), String.format("(,|^)%1$s(,|$)", agent.getType()))) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_AGENT_NOT_SUPPORTED.name());
            return false;
        } else {
            return true;
        }
    }

}
