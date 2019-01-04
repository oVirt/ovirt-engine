package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.utils.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "84e6ddee-ab0d-42dd-82f0-c298889db567",
        name = "InClusterUpgrade",
        description = "Filter out all hosts which run an older OS version than the host the vm is currently running on.",
        type = PolicyUnitType.FILTER
)
public class InClusterUpgradeFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(InClusterUpgradeFilterPolicyUnit.class);

    public InClusterUpgradeFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {

        final VdsDynamic sourceHost = getLastHost(vm);
        if (sourceHost == null) {
            return hosts;
        }
        final OS lastHostOs = OS.fromPackageVersionString(sourceHost.getHostOs());

        if (!lastHostOs.isValid()) {
            log.debug("Source host {} does not provide a valid and complete OS identifier. Found {}.",
                    sourceHost.getId(),
                    sourceHost.getHostOs());
            return hosts;
        }

        final List<VDS> notOlderHosts = new ArrayList<>();
        for (VDS host : hosts) {
            final OS hostOs = OS.fromPackageVersionString(host.getHostOs());
            if (!hostOs.isValid()) {
                log.debug("Host {} does not provide a valid OS identifier. Found {}.",
                        host.getId(),
                        host.getHostOs());
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__INVALID_OS.name());
                messages.addMessage(host.getId(), String.format("$os %1$s", host.getHostOs()));
            } else if (!hostOs.isSameOsFamily(lastHostOs)) {
                log.debug("Host {} does not run the same operating system. Expected {}, found {}",
                        host.getId(),
                        lastHostOs.getName(),
                        hostOs.getName());
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__WRONG_OS.name());
                messages.addMessage(host.getId(), String.format("$expected %1$s", lastHostOs.getName()));
                messages.addMessage(host.getId(), String.format("$found %1$s", hostOs.getName()));
            } else if (hostOs.isOlderThan(lastHostOs) && !hostOs.isSameMajorVersion(lastHostOs)) {
                log.debug("Host {} runs a too old OS version. Found {}",
                        host.getId(),
                        host.getHostOs());
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__OLD_OS.name());
                messages.addMessage(host.getId(), String.format("$found %1$s", hostOs.getName()));
            } else {
                notOlderHosts.add(host);
            }
        }
        return notOlderHosts;
    }
}
