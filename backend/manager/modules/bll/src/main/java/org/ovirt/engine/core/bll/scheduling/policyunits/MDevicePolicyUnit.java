package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.MDevType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.MDevTypesUtils;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "aa604ea6-f758-4d28-8e05-bf5f4a0818bd",
        name = "MDevice",
        type = PolicyUnitType.FILTER,
        description = "Filters out hosts that do not have required mDev devices"
)
public class MDevicePolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(MDevicePolicyUnit.class);

    @Inject
    private HostDeviceDao hostDeviceDao;

    public MDevicePolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {
        List<String> vmMDevs = MDevTypesUtils.getMDevTypes(vm);
        if (vmMDevs.isEmpty()) {
            return hosts;
        }

        List<VDS> list = new ArrayList<>();
        for (VDS host : hosts) {
            Map<String, List<MDevType>> hostMDevs = getMDevsForHost(host);

            List<String> missingMDevs = getMissingMDevsForHost(hostMDevs, vmMDevs);
            if (!missingMDevs.isEmpty()) {
                messages.addMessage(host.getId(), String.format("$missingMDevs %1$s", StringUtils.join(missingMDevs, ",")));
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__HOST_MDEV_DEVICE_MISSING.toString());
                log.debug("Host '{}' is missing required mDev devices ({}))",
                        host.getName(),
                        StringUtils.join(missingMDevs, ", "));
                continue;
            }

            List<String> unavailableMDevs = getUnavailableMDevsForHost(hostMDevs, vmMDevs);
            if (!unavailableMDevs.isEmpty()) {
                messages.addMessage(host.getId(), String.format("$unavailableMDevs %1$s", StringUtils.join(unavailableMDevs, ",")));
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__HOST_MDEV_DEVICE_UNAVAILABLE.toString());
                log.debug("Host '{}' has required mDev devices with no available instances ({}))",
                        host.getName(),
                        StringUtils.join(missingMDevs, ", "));
                continue;
            }
            list.add(host);
        }
        return list;
    }

    private Map<String, List<MDevType>> getMDevsForHost(VDS host) {
        List<HostDevice> devices = hostDeviceDao.getHostDevicesByHostId(host.getId());
        Map<String, List<MDevType>> mDevsMap = new HashMap<>();
        for (HostDevice device : devices) {
            if (device.getMdevTypes() == null || device.getMdevTypes().isEmpty()) {
                continue;
            }
            List<MDevType> mdevList = device.getMdevTypes();
            for (MDevType mdev : mdevList) {
                if (!mDevsMap.containsKey(mdev.getName())) {
                    mDevsMap.put(mdev.getName(), new ArrayList<MDevType>());
                }
                mDevsMap.get(mdev.getName()).add(mdev);
            }
        }
        return mDevsMap;
    }

    private List<String> getMissingMDevsForHost(Map<String, List<MDevType>> hostMDevs, List<String> vmMDevs) {
        List<String> missingMDevs = new ArrayList<>();
        for (String vmMDev : vmMDevs) {
            if (!hostMDevs.containsKey(vmMDev)) {
                missingMDevs.add(vmMDev);
            }
        }
        return missingMDevs;
    }

    private List<String> getUnavailableMDevsForHost(Map<String, List<MDevType>> hostMDevs, List<String> vmMDevs) {
        List<String> unavailableMDevs = new ArrayList<>();
        for (String vmMDev : vmMDevs) {
            List<MDevType> mDevs = hostMDevs.get(vmMDev);
            boolean isAvailable = false;
            for (MDevType mDev : mDevs) {
                if (mDev.getAvailableInstances() > 0) {
                    isAvailable = true;
                    break;
                }
            }
            if (!isAvailable) {
                unavailableMDevs.add(vmMDev);
            }
        }
        return unavailableMDevs;
    }
}
