package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.dao.VdsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "e659c871-0bf1-4ccc-b748-f28f5d08dffd",
        name = "HA",
        description = "Runs the hosted engine VM only on hosts with a positive score",
        type = PolicyUnitType.FILTER
)
public class HostedEngineHAClusterFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(HostedEngineHAClusterFilterPolicyUnit.class);

    private static final int MIGRATION_THRESHOLD_SCORE = 800;

    @Inject
    private VdsDao vdsDao;

    public HostedEngineHAClusterFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {

        // The filter is relevant only for Hosted Engine VM
        if (vm.isHostedEngine()) {
            int haScoreMin = getMinScore(vm);
            List<VDS> hostsToRunOn = new ArrayList<>();
            for (VDS host : hosts) {
                if (!host.isHostedEngineConfigured()) {
                    log.debug("Host '{}' was filtered out as it is not a Hosted Engine host.", host.getName());
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__NOT_HE_HOST.name());
                    continue;
                }

                int haScore = host.getHighlyAvailableScore();
                if (haScore <= 0) {
                    log.debug("Host '{}' was filtered out as it doesn't have a positive score (the score is {})",
                            host.getName(), haScore);
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__HE_HOST_NOT_POSITIVE_SCORE.name());
                    continue;
                }

                if (haScore <= haScoreMin) {
                    log.debug("Host '{}' was filtered out as it has much lower score than the current host (the score is {}, must be more than {})",
                            host.getName(), haScore, haScoreMin);
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__HE_HOST_LOW_SCORE.name());
                    continue;
                }

                hostsToRunOn.add(host);
                log.debug("Host '{}' wasn't filtered out as it has a score of {}",
                        host.getName(),
                        haScore);
            }

            return hostsToRunOn;
        } else {
            return hosts;
        }
    }

    private int getMinScore(VM vm) {
        if (vm.getRunOnVds() == null) {
            return 0;
        }

        VDS currentHost = vdsDao.get(vm.getRunOnVds());
        if (currentHost.getHighlyAvailableGlobalMaintenance()) {
            return 0;
        }

        return Math.max(currentHost.getHighlyAvailableScore() - MIGRATION_THRESHOLD_SCORE, 0);
    }
}
