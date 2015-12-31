package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@Singleton
public class VfSchedulerImpl implements VfScheduler {

    private NetworkDao networkDao;

    private InterfaceDao interfaceDao;

    private HostDeviceDao hostDeviceDao;

    private VmDeviceDao vmDeviceDao;

    private NetworkDeviceHelper networkDeviceHelper;

    private Map<Guid, Map<Guid, Map<Guid, String>>> vmToHostToVnicToVfMap = new ConcurrentHashMap<>();

    @Inject
    public VfSchedulerImpl(NetworkDao networkDao,
            InterfaceDao interfaceDao,
            HostDeviceDao hostDeviceDao,
            VmDeviceDao vmDeviceDao,
            NetworkDeviceHelper networkDeviceHelper) {
        this.networkDao = networkDao;
        this.interfaceDao = interfaceDao;
        this.hostDeviceDao = hostDeviceDao;
        this.vmDeviceDao = vmDeviceDao;
        this.networkDeviceHelper = networkDeviceHelper;
    }

    @Override
    public List<String> validatePassthroughVnics(Guid vmId, Guid hostId,
            List<VmNetworkInterface> vnics) {

        List<VmNetworkInterface> pluggedPassthroughVnics = getPluggedPassthroughVnics(vnics);

        if (pluggedPassthroughVnics.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Guid, Map<Guid, String>> hostToVnicToVfMap = vmToHostToVnicToVfMap.get(vmId);
        if (hostToVnicToVfMap == null) {
            hostToVnicToVfMap = new HashMap<>();
            vmToHostToVnicToVfMap.put(vmId, hostToVnicToVfMap);
        }

        Map<Guid, List<String>> nicToUsedVfs = new HashMap<>();
        Map<Guid, VdsNetworkInterface> fetchedNics = new HashMap<>();
        List<String> problematicVnics = new ArrayList<>();
        List<HostNicVfsConfig> vfsConfigs =
                networkDeviceHelper.getHostNicVfsConfigsWithNumVfsDataByHostId(hostId);

        Map<Guid, String> vnicToVfMap = new HashMap<>();
        hostToVnicToVfMap.put(hostId, vnicToVfMap);

        for (final VmNetworkInterface vnic : pluggedPassthroughVnics) {
            String freeVf = null;
            freeVf = findFreeVfForVnic(vfsConfigs, vnic, nicToUsedVfs, fetchedNics, true);

            if (freeVf == null) {
                problematicVnics.add(vnic.getName());
            } else {
                vnicToVfMap.put(vnic.getId(), freeVf);
            }
        }

        return problematicVnics;
    }

    private List<VmNetworkInterface> getPluggedPassthroughVnics(List<VmNetworkInterface> vnics) {
        return LinqUtils.filter(vnics, new Predicate<VmNetworkInterface>() {

            @Override
            public boolean eval(VmNetworkInterface vnic) {
                return vnic.isPassthrough() && vnic.isPlugged();
            }
        });
    }

   private String findFreeVfForVnic(List<HostNicVfsConfig> vfsConfigs,
            final VmNetworkInterface vnic,
            Map<Guid, List<String>> nicToUsedVfs,
            Map<Guid, VdsNetworkInterface> fetchedNics,
            boolean shouldCheckDirectlyAttachedVmDevices) {
        Guid vmId = vnic.getVmId();
        Network vnicNetwork =
                vnic.getNetworkName() == null ? null : networkDao.getByName(vnic.getNetworkName());
        for (HostNicVfsConfig vfsConfig : vfsConfigs) {
            if (vfsConfig.getNumOfVfs() != 0 && isNetworkInVfsConfig(vnicNetwork, vfsConfig)) {
                List<String> skipVfs = new ArrayList<>();
                HostDevice freeVf =
                        getFreeVf(vfsConfig, nicToUsedVfs, fetchedNics, vmId, shouldCheckDirectlyAttachedVmDevices, skipVfs);
                while (freeVf != null && (isSharingIommuGroup(freeVf)
                        || (shouldCheckDirectlyAttachedVmDevices
                                && shouldBeDirectlyAttached(freeVf.getName(), vmId)))) {
                    skipVfs.add(freeVf.getName());
                    freeVf = getFreeVf(vfsConfig,
                            nicToUsedVfs,
                            fetchedNics,
                            vmId,
                            shouldCheckDirectlyAttachedVmDevices,
                            skipVfs);
                }
                if (freeVf != null) {
                    return freeVf.getName();
                }
            }
        }
        return null;
    }

    private boolean isNetworkInVfsConfig(Network vnicNetwork, HostNicVfsConfig vfsConfig) {
        if (vnicNetwork == null) {
            return true;
        }

        boolean isNetworkInConfig =
                vfsConfig.isAllNetworksAllowed() || vfsConfig.getNetworks().contains(vnicNetwork.getId());
        boolean isLabelInConfig =
                vnicNetwork.getLabel() != null && vfsConfig.getNetworkLabels().contains(vnicNetwork.getLabel());

        return isNetworkInConfig || isLabelInConfig;
    }

    private HostDevice getFreeVf(HostNicVfsConfig hostNicVfsConfig,
            Map<Guid, List<String>> nicToUsedVfs,
            Map<Guid, VdsNetworkInterface> fetchedNics,
            Guid vmId,
            boolean shouldCheckDirectlyAttachedVmDevices,
            List<String> skipVfs) {
        VdsNetworkInterface nic = getNic(hostNicVfsConfig.getNicId(), fetchedNics);
        List<String> usedVfsByNic = nicToUsedVfs.get(nic.getId());

        if (usedVfsByNic != null) {
            skipVfs.addAll(usedVfsByNic);
        }

        HostDevice freeVf = networkDeviceHelper.getFreeVf(nic, skipVfs);

        if (freeVf != null) {

            if (usedVfsByNic == null) {
                usedVfsByNic = new ArrayList<>();
                nicToUsedVfs.put(nic.getId(), usedVfsByNic);
            }
            usedVfsByNic.add(freeVf.getDeviceName());

            return freeVf;
        }

        return null;
    }

    private VdsNetworkInterface getNic(Guid nicId, Map<Guid, VdsNetworkInterface> fetchedNics) {
        VdsNetworkInterface nic = fetchedNics.get(nicId);
        if (nic == null) {
            nic = interfaceDao.get(nicId);
            fetchedNics.put(nicId, nic);
        }

        return nic;
    }

    @Override
    public Map<Guid, String> getVnicToVfMap(Guid vmId, Guid hostId) {
        Map<Guid, Map<Guid, String>> hostToVnicToVfMap = vmToHostToVnicToVfMap.get(vmId);
        return hostToVnicToVfMap == null ? null : hostToVnicToVfMap.get(hostId);
    }

    @Override
    public void cleanVmData(Guid vmId) {
        vmToHostToVnicToVfMap.remove(vmId);
    }

    private boolean isSharingIommuGroup(HostDevice device) {
        if (device.getIommuGroup() == null) {
            return false;
        }

        // Check that the device doesn't share iommu group with other devices
        List<HostDevice> iommoGroupDevices =
                hostDeviceDao.getHostDevicesByHostIdAndIommuGroup(device.getHostId(), device.getIommuGroup());

        return iommoGroupDevices.size() > 1;
    }

    private boolean shouldBeDirectlyAttached(String vfName, Guid vmId) {
        return !vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(vmId, VmDeviceGeneralType.HOSTDEV, vfName).isEmpty();
    }
}
