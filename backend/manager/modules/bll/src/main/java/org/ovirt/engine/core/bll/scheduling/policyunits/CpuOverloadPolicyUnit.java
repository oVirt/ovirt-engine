package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.HostCpuLoadHelper;
import org.ovirt.engine.core.bll.scheduling.utils.VdsCpuUnitPinningHelper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "98842bc5-4094-4b83-8224-7b50f86a94c9",
        name = "CPUOverloaded",
        type = PolicyUnitType.FILTER,
        description = "Filters out CPU overloaded hosts.",
        parameters = {
            PolicyUnitParameter.HIGH_UTILIZATION,
            PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES
        }
)
public class CpuOverloadPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(CpuOverloadPolicyUnit.class);

    @Inject
    protected ResourceManager resourceManager;

    @Inject
    protected VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper;

    public CpuOverloadPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup, PerHostMessages messages) {
        List<VDS> list = new ArrayList<>();

        final int highUtilization = NumberUtils.toInt(context.getPolicyParameters().get(PolicyUnitParameter.HIGH_UTILIZATION.getDbName()),
                getHighUtilizationDefaultValue());

        final int cpuOverCommitDurationMinutes =
                NumberUtils.toInt(context.getPolicyParameters().get(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES.getDbName()),
                        Config.<Integer>getValue(ConfigValues.CpuOverCommitDurationMinutes));

        for (VDS vds : hosts) {

            HostCpuLoadHelper cpuLoadHelper = new HostCpuLoadHelper(vds,
                    resourceManager,
                    vdsCpuUnitPinningHelper,
                    context.getCluster().getCountThreadsAsCores());

            if (!cpuLoadHelper.hostStatisticsPresent()) {
                continue;
            }

            // This has to use greater than (without 'or equals') as we only have percents without
            // fractional part and 100% means we still have some power on machines with more
            // than 100 cpus
            double hostSharedLoad = cpuLoadHelper.getEffectiveSharedCpuLoad();
            if (hostSharedLoad > highUtilization
                    && vds.getCpuOverCommitTimestamp() != null
                    && (getTime().getTime() - vds.getCpuOverCommitTimestamp().getTime()) >= TimeUnit.MINUTES
                            .toMillis(cpuOverCommitDurationMinutes)) {

                messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__CPU_OVERLOADED.toString());
                log.debug("Host '{}' is too loaded ({}%) and has been overloaded since {}.",
                        vds.getName(),
                        Math.round(hostSharedLoad),
                        vds.getCpuOverCommitTimestamp().toString());
                continue;
            }

            list.add(vds);
        }
        return list;
    }

    protected Date getTime() {
        return new Date();
    }

    protected static int getHighUtilizationDefaultValue() {
        return Config.<Integer> getValue(ConfigValues.HighUtilizationForScheduling);
    }
}
