package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
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
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters hosts based on the passthroughSupported flag when the VM requires hostdev passthrough
 */
@SchedulingUnit(
        guid = "728a21f1-f97e-4d32-bc3e-b3cc49756abb",
        name = "HostDevice",
        description = "Filters out hosts not supporting VM required host devices",
        type = PolicyUnitType.FILTER
)
public class HostDeviceFilterPolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(HostDeviceFilterPolicyUnit.class);

    @Inject
    private HostDeviceManager hostDeviceManager;

    public HostDeviceFilterPolicyUnit(PolicyUnit policyUnit, PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {

        // noop if VM does not require host devices
        if (!hostDeviceManager.checkVmNeedsDirectPassthrough(vm)) {
            return hosts;
        }

        boolean hasPciDevices = hostDeviceManager.checkVmNeedsPciDevices(vm.getId());

        // In case of direct host device passthrough the 'dedicatedVmForVdsList' contains exactly 1 element.
        Guid hostId = vm.getDedicatedVmForVdsList().get(0);

        List<VDS> list = new ArrayList<>();
        for (VDS host : hosts) {
            if (hasPciDevices && !host.isHostDevicePassthroughEnabled()) {
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__HOSTDEV_DISABLED.toString());
                log.debug("Host '{}' does not support host device passthrough", host.getName());
                continue;
            }
            if (!hostId.equals(host.getId())) {
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__WRONG_HOST_FOR_REQUESTED_HOST_DEVICES.toString());
                log.debug("Host '{}' does not provide required host devices", host.getName());
                continue;
            }
            if (!hostDeviceManager.checkVmHostDeviceAvailability(vm, host.getId())) {
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__HOST_DEVICE_UNAVAILABLE.toString());
                log.debug("Some of the devices on host '{}' are unavailable", host.getName());
                continue;
            }
            list.add(host);
        }

        return list;
    }

}
