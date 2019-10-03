package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "b454ae40-f767-45b1-949a-7e5bd04d83ab",
        name = "VM leases ready",
        type = PolicyUnitType.FILTER,
        description = "Filters out hosts that are not ready to support VM leases"
)
public class VmLeasesReadyFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(VmLeasesReadyFilterPolicyUnit.class);

    @Inject
    private ResourceManager resourceManager;

    public VmLeasesReadyFilterPolicyUnit(PolicyUnit policyUnit,
                                 PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {

        if (vm.getLeaseStorageDomainId() == null) {
            return hosts;
        }

        return hosts.stream()
                .filter(vds -> {
                    ArrayList<VDSDomainsData> domainsData = resourceManager.getVdsManager(vds.getId()).getDomains();
                    if (!isVmLeaseReadyForHost(domainsData, vm, vds.getName())) {
                        messages.addMessage(vds.getId(),
                                EngineMessage.ACTION_TYPE_FAILED_VM_LEASE_IS_NOT_READY_FOR_HOST.toString());
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList());
    }

    private boolean isVmLeaseReadyForHost(ArrayList<VDSDomainsData> domainsData, VM vm, String vdsName) {
        if (domainsData == null) {
            log.debug("No domain data, skipping host {}", vdsName);
            return false;
        }

        Optional<VDSDomainsData> leaseDomainsData = domainsData.stream()
                .filter(domainData -> domainData.getDomainId().equals(vm.getLeaseStorageDomainId()))
                .filter(VDSDomainsData::isAcquired).findFirst();
        return leaseDomainsData.isPresent();
    }
}
