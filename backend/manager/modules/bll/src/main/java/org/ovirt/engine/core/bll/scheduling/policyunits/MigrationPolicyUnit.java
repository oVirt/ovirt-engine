package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
        guid = "e659c871-0bf1-4ccc-b748-f28f5d08ddda",
        name = "Migration",
        description = "Prevent migration to the same host.",
        type = PolicyUnitType.FILTER
)
public class MigrationPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(MigrationPolicyUnit.class);

    @Inject
    private VdsDao vdsDao;

    public MigrationPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    private boolean validateDestinationVdsAddress(VDS srcVds, VDS dstVds) {
        InetAddress dstAddress = null;

        try {
            dstAddress = InetAddress.getByName(dstVds.getHostName());
        } catch (UnknownHostException e) {
            // no clues, better to not guess. Let libvirt sort out the mess,
            // as it already did before.
            return true;
        }

        if (dstAddress != null && dstAddress.isLoopbackAddress()) {
            log.warn("cannot migrate to VDS {}: resolved to loopback address", dstVds.getHostName());
            return false;
        }

        if (srcVds != null) { // on runVm will be null
            InetAddress srcAddress = null;

            if (srcVds.getHostName().equals(dstVds.getHostName())) {
                log.warn("cannot migrate to VDS {}: same hostname as source", dstVds.getHostName());
                return false;
            }

            try {
                srcAddress = InetAddress.getByName(srcVds.getHostName());
            } catch (UnknownHostException e) {
                return true; // same as per dst address
            }

            if (srcAddress != null && srcAddress.equals(dstAddress)) {
                log.warn("cannot migrate to VDS {}: resolved address equal to source", dstVds.getHostName());
                return false;
            }
        }

        return true;
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {

        if (vm.getRunOnVds() != null) {
            List<VDS> hostsToRunOn = new ArrayList<>();
            VDS srcVds = vdsDao.get(vm.getRunOnVds());

            for (VDS host : hosts) {
                if (host.getId().equals(vm.getRunOnVds())) {
                    log.debug("Vm '{}' already runs on host '{}',"
                                    + " the host is not filtered out for balancing purposes",
                            vm.getName(), host.getName());
                    hostsToRunOn.add(host);
                    continue;
                }

                if (!validateDestinationVdsAddress(srcVds, host)) {
                    log.debug("Host '{}' resolves to the same machine as host '{}', filtering out", srcVds.getName(), host.getName());
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__SAME_HOST.toString());
                    continue;
                }

                hostsToRunOn.add(host);
            }

            return hostsToRunOn;
        }

        return hosts;
    }
}
