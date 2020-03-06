package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "43b9e654-337f-4b0d-a896-cbb22ce5a7fc",
        name = "Swap",
        description = "Filters out hosts that are swapping",
        type = PolicyUnitType.FILTER,
        parameters = PolicyUnitParameter.MAX_ALLOWED_SWAP_USAGE
)
public class SwapFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(SwapFilterPolicyUnit.class);

    public SwapFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context,
            List<VDS> hosts,
            VM vm,
            PerHostMessages messages) {

        if (!Config.<Boolean> getValue(ConfigValues.EnableSwapCheck)) {
            return hosts; // Ignore swap usage
        }

        // If VM in Paused mode - no additional memory allocation needed
        if (vm.getStatus() == VMStatus.Paused) {
            return hosts;
        }

        List<VDS> goodHosts = new ArrayList<>();

        final int allowedSwapUsage =
                NumberUtils.toInt(context.getPolicyParameters().get(PolicyUnitParameter.MAX_ALLOWED_SWAP_USAGE.getDbName()),
                        Config.<Integer>getValue(ConfigValues.BlockMigrationOnSwapUsagePercentage));

        for (VDS vds : hosts) {
            if (isHostSwapping(vds, allowedSwapUsage)) {
                log.debug("Host '{}' is swapping more than allowed. Ignoring it.", vds.getName());
                messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__SWAP_VALUE_ILLEGAL.toString());
                continue;
            }

            goodHosts.add(vds);
        }

        return goodHosts;
    }

    /**
     * Determines whether a host is swapping more than the defined threshold.
     *
     * @param host The host.
     * @param allowedSwapUsage The allowed percentage of swap before we consider
     *                         the host to be swapping
     * @return true if the host is swapping; otherwise false.
     */
    private boolean isHostSwapping(VDS host, int allowedSwapUsage) {
        if (host.getSwapTotal() == null || host.getSwapFree() == null || host.getMemFree() == null
                || host.getMemFree() <= 0 || host.getPhysicalMemMb() == null || host.getPhysicalMemMb() <= 0) {
            return false; // No swap information available
        }

        long swapTotal = host.getSwapTotal();
        long swapFree = host.getSwapFree();
        long memFree = host.getMemFree();
        long physicalMemMb = host.getPhysicalMemMb();

        // Compute how much swap is used (taking into consideration available
        // RAM) as a percentage from physical memory
        return ((swapTotal - swapFree - memFree) * 100 / physicalMemMb) > allowedSwapUsage;
    }
}
