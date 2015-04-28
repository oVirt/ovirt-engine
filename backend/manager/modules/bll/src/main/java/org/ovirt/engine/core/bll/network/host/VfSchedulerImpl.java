package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@Singleton
public class VfSchedulerImpl implements VfScheduler {

    private NetworkDao networkDao;

    private InterfaceDao interfaceDao;

    private HostDeviceDao hostDeviceDao;

    private HostNicVfsConfigHelper vfsConfigHelper;

    private Map<Guid, Map<Guid, Map<Guid, String>>> vmToHostToVnicToVfMap = new ConcurrentHashMap<>();

    @Inject
    public VfSchedulerImpl(NetworkDao networkDao,
            InterfaceDao interfaceDao,
            HostDeviceDao hostDeviceDao,
            HostNicVfsConfigHelper vfsConfigHelper) {
        this.networkDao = networkDao;
        this.interfaceDao = interfaceDao;
        this.hostDeviceDao = hostDeviceDao;
        this.vfsConfigHelper = vfsConfigHelper;
    }

    @Override
    public List<String> validatePassthroughVnics(Guid vmId, Guid hostId,
            List<VmNetworkInterface> vnics) {

        Map<Guid, Map<Guid, String>> hostToVnicToVfMap = new HashMap<>();
        vmToHostToVnicToVfMap.put(vmId, hostToVnicToVfMap);
        Map<Guid, List<String>> nicToUsedVfs = new HashMap<>();
        Map<Guid, VdsNetworkInterface> fetchedNics = new HashMap<>();
        List<String> problematicVnics = new ArrayList<>();
        List<HostNicVfsConfig> vfsConfigs =
                vfsConfigHelper.getHostNicVfsConfigsWithNumVfsDataByHostId(hostId);

        Map<Guid, String> vnicToVfMap = new HashMap<>();
        hostToVnicToVfMap.put(hostId, vnicToVfMap);

        for (final VmNetworkInterface vnic : vnics) {
            String freeVf = null;
            if (vnic.isPassthrough()) {
                freeVf = findFreeVfForVnic(vfsConfigs, vnic, nicToUsedVfs, fetchedNics);
                if (freeVf == null) {
                    problematicVnics.add(vnic.getName());
                } else {
                    vnicToVfMap.put(vnic.getId(), freeVf);
                }
            }
        }

        return problematicVnics;
    }

    private String findFreeVfForVnic(List<HostNicVfsConfig> vfsConfigs,
            final VmNetworkInterface vnic,
            Map<Guid, List<String>> nicToUsedVfs,
            Map<Guid, VdsNetworkInterface> fetchedNics) {
        Network vnicNetwork =
                vnic.getNetworkName() == null ? null : networkDao.getByName(vnic.getNetworkName());
        for (HostNicVfsConfig vfsConfig : vfsConfigs) {
            if (vfsConfig.getNumOfVfs() != 0 && isNetworkInVfsConfig(vnicNetwork, vfsConfig)) {
                String freeVf = getFreeVf(vfsConfig, nicToUsedVfs, fetchedNics);
                if (freeVf != null) {
                    return freeVf;
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

    private String getFreeVf(HostNicVfsConfig hostNicVfsConfig,
            Map<Guid, List<String>> nicToUsedVfs,
            Map<Guid, VdsNetworkInterface> fetchedNics) {
        VdsNetworkInterface nic = getNic(hostNicVfsConfig.getNicId(), fetchedNics);
        List<String> usedVfsByNic = nicToUsedVfs.get(nic.getId());
        HostDevice freeVf = vfsConfigHelper.getFreeVf(nic, usedVfsByNic);

        if (freeVf != null) {
            if (isSharingIommuGroup(freeVf)) {
                return null;
            }

            String vfName = freeVf.getDeviceName();

            if (usedVfsByNic == null) {
                usedVfsByNic = new ArrayList<String>();
                nicToUsedVfs.put(nic.getId(), usedVfsByNic);
            }
            usedVfsByNic.add(vfName);

            return vfName;
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
        // Check that the device doesn't share iommu group with other devices
        List<HostDevice> iommoGroupDevices =
                hostDeviceDao.getHostDevicesByHostIdAndIommuGroup(device.getHostId(), device.getIommuGroup());

        return iommoGroupDevices.size() > 1;
    }
}
