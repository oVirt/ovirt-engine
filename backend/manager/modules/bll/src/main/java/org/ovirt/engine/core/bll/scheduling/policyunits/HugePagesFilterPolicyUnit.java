package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingHugePages;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.HugePage;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.HugePageUtils;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "23e07b34-66dd-4735-bc45-bcddded02c05",
        name = "HugePages",
        type = PolicyUnitType.FILTER,
        description = "Filters out hosts that do not have enough free huge pages"
)
public class HugePagesFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(HugePagesFilterPolicyUnit.class);

    public HugePagesFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context,
            List<VDS> hosts,
            List<VM> vmGroup,
            PerHostMessages messages) {

        List<VM> vmsWithHugepages = vmGroup.stream()
                .filter(vm -> HugePageUtils.isBackedByHugepages(vm.getStaticData()))
                .collect(Collectors.toList());

        if (vmsWithHugepages.isEmpty()) {
            return new ArrayList<>(hosts);
        }

        List<VDS> newHosts = new ArrayList<>(hosts.size());
        for (VDS host: hosts) {
            Map<Integer, Integer> requiredPages = vmGroup.stream()
                    .filter(vm -> !host.getId().equals(vm.getRunOnVds()))
                    .map(vm -> HugePageUtils.getHugePages(vm.getStaticData()))
                    .flatMap(m -> m.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));

            Map<Integer, Integer> availablePages = subtractMaps(prepareHugePageMap(host),
                    PendingHugePages.collectForHost(getPendingResourceManager(), host.getId()));

            if (!requiredPages.entrySet().stream()
                    .allMatch(pg -> availablePages.getOrDefault(pg.getKey(), 0) >= pg.getValue())) {
                if (log.isDebugEnabled()) {
                    List<Guid> vmIds = vmGroup.stream()
                            .map(VM::getId)
                            .collect(Collectors.toList());

                    log.debug("Host {} does not have enough free hugepages for VM group {}", host.getId(), vmIds);
                }
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_HUGE_PAGES.name());
            } else {
                newHosts.add(host);
            }
        }

        return newHosts;
    }

    private Map<Integer, Integer> prepareHugePageMap(VDS host) {
        List<HugePage> reportedHugePages = host.getHugePages();
        Map<Integer, Integer> hugePages = new HashMap<>(reportedHugePages.size());
        for (HugePage hp: reportedHugePages) {
            hugePages.put(hp.getSizeKB(), hp.getAmount());
        }
        return Collections.unmodifiableMap(hugePages);
    }

    private Map<Integer, Integer> subtractMaps(Map<Integer, Integer> from,
            Map<Integer, Integer> amount) {
        Map<Integer, Integer> result = new HashMap<>(from);
        for (Map.Entry<Integer, Integer> subs: amount.entrySet()) {
            result.compute(subs.getKey(), (key, val) -> (val == null ? 0 : val) - subs.getValue());
        }
        return Collections.unmodifiableMap(result);
    }
}
