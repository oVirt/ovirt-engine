package org.ovirt.engine.core.bll.network.host;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.NetworkUtils;

@Singleton
class NetworkDeviceHelperImpl implements NetworkDeviceHelper {

    private final InterfaceDao interfaceDao;
    private final HostDeviceDao hostDeviceDao;
    private final HostNicVfsConfigDao hostNicVfsConfigDao;

    @Inject
    NetworkDeviceHelperImpl(InterfaceDao interfaceDao,
            HostDeviceDao hostDeviceDao,
            HostNicVfsConfigDao hostNicVfsConfigDao) {
        Objects.requireNonNull(interfaceDao, "interfaceDao cannot be null");
        Objects.requireNonNull(hostDeviceDao, "hostDeviceDao cannot be null");
        Objects.requireNonNull(hostNicVfsConfigDao, "hostNicVfsConfigDao cannot be null");

        this.interfaceDao = interfaceDao;
        this.hostDeviceDao = hostDeviceDao;
        this.hostNicVfsConfigDao = hostNicVfsConfigDao;
    }

    @Override
    public VdsNetworkInterface getNicByPciDevice(final HostDevice pciDevice) {
        return getNicByPciDevice(pciDevice, null);
    }

    @Override
    public VdsNetworkInterface getNicByPciDevice(final HostDevice pciDevice, final Collection<HostDevice> devices) {
        return getNicByPciDevice(pciDevice, devices, null);
    }

    private VdsNetworkInterface getNicByPciDevice(final HostDevice pciDevice,
            final Collection<HostDevice> devices,
            final Collection<VdsNetworkInterface> hostNics) {
        final HostDevice netDevice = getFirstChildNetworkDevice(pciDevice, devices);

        if (netDevice == null) {
            return null;
        }

        final Collection<VdsNetworkInterface> hostInterfaces =
                hostNics == null ? interfaceDao.getAllInterfacesForVds(netDevice.getHostId()) : hostNics;

        return hostInterfaces.stream().filter(iface -> iface.getName().equals(netDevice.getNetworkInterfaceName()))
                .findFirst().orElse(null);
    }

    private HostDevice getFirstChildNetworkDevice(final HostDevice pciDevice, final Collection<HostDevice> devices) {
        Collection<HostDevice> hostDevices = devices == null ? getDevicesByHostId(pciDevice.getHostId()) : devices;
        return hostDevices.stream()
                .filter(device -> pciDevice.getDeviceName().equals(device.getParentDeviceName())
                        && isNetworkDevice(device))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isSriovDevice(HostDevice device) {
        return device.getTotalVirtualFunctions() != null;
    }

    @Override
    public boolean isNetworkDevice(HostDevice device) {
        return device.getNetworkInterfaceName() != null;
    }

    @Override
    public void updateHostNicVfsConfigWithNumVfsData(HostNicVfsConfig hostNicVfsConfig) {
        VdsNetworkInterface nic = getNicById(hostNicVfsConfig.getNicId());

        updateVfsConfigWithNumOfVfsData(hostNicVfsConfig,
                nic,
                getDevicesByHostId(nic.getVdsId()));
    }

    @Override
    public List<HostNicVfsConfig> getHostNicVfsConfigsWithNumVfsDataByHostId(Guid hostId) {
        List<HostNicVfsConfig> hostNicVfsConfigList = hostNicVfsConfigDao.getAllVfsConfigByHostId(hostId);
        List<HostDevice> deviceList = getDevicesByHostId(hostId);

        for (HostNicVfsConfig hostNicVfsConfig : hostNicVfsConfigList) {
            updateVfsConfigWithNumOfVfsData(hostNicVfsConfig, null, deviceList);
        }

        return hostNicVfsConfigList;
    }

    private void updateVfsConfigWithNumOfVfsData(HostNicVfsConfig hostNicVfsConfig,
            VdsNetworkInterface nic,
            List<HostDevice> deviceList) {
        if (nic == null) {
            nic = getNicById(hostNicVfsConfig.getNicId());
        }

        HostDevice pciDevice = getPciDeviceByNic(nic, deviceList);

        hostNicVfsConfig.setMaxNumOfVfs(getMaxNumOfVfs(pciDevice));
        hostNicVfsConfig.setNumOfVfs(getNumOfVfs(pciDevice, deviceList));
        hostNicVfsConfig.setNumOfFreeVfs(getNumOfFreeVfs(pciDevice, deviceList));
    }

    private int getNumOfFreeVfs(HostDevice pciDevice, List<HostDevice> deviceList) {
        return getVfs(pciDevice, deviceList).stream().filter(this::isVfFree).mapToInt(device -> 1).sum();
    }

    private HostDevice getPciDeviceByNic(final VdsNetworkInterface nic, List<HostDevice> deviceList) {
        return getPciDeviceByNic(nic, deviceList, Entities.entitiesByName(deviceList));
    }

    private HostDevice getPciDeviceByNic(final VdsNetworkInterface nic,
            List<HostDevice> deviceList,
            Map<String, HostDevice> devicesByName) {
        final String nicName = nic.getName();
        final HostDevice netDevice = deviceList.stream()
                .filter(device -> nicName.equals(device.getNetworkInterfaceName())).findFirst().orElse(null);

        Objects.requireNonNull(netDevice,
                String.format("Host \"%s\": nic \"%s\" doesn't have a net device", nic.getVdsName(), nicName));

        final String parentDeviceName = netDevice.getParentDeviceName();
        final HostDevice pciDevice = devicesByName.get(parentDeviceName);

        Objects.requireNonNull(pciDevice,
                String.format("Host \"%s\": net device \"%s\" doesn't have a parent pci device \"%s\"",
                        nic.getVdsName(),
                        netDevice.getName(),
                        parentDeviceName));

        return pciDevice;
    }

    private int getMaxNumOfVfs(HostDevice pciDevice) {
        return pciDevice.getTotalVirtualFunctions();
    }

    private int getNumOfVfs(HostDevice pciDevice, List<HostDevice> deviceList) {
        List<HostDevice> vfs = getVfs(pciDevice, deviceList);

        return vfs.size();
    }

    private VdsNetworkInterface getNicById(Guid nicId) {
        return interfaceDao.get(nicId);
    }

    private List<HostDevice> getDevicesByHostId(Guid hostId) {
        return hostDeviceDao.getHostDevicesByHostId(hostId);
    }

    private List<HostDevice> getVfs(final HostDevice pciDevice, List<HostDevice> deviceList) {
        return deviceList.stream()
                .filter(device -> pciDevice.getDeviceName().equals(device.getParentPhysicalFunction()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean areAllVfsFree(VdsNetworkInterface nic) {
        HostDevice nonFreeVf = getVf(nic, false);

        return nonFreeVf == null;
    }

    @Override
    public boolean isDeviceNetworkFree(HostDevice hostDevice) {
        HostDevice firstChildNetDevice = getFirstChildNetworkDevice(hostDevice, null);
        if (firstChildNetDevice == null) {
            return true;
        }

        return isNetworkDeviceFree(hostDevice);
    }

    private boolean isVfFree(HostDevice vf) {
        // Check if the VF is attached directly to a VM
        if (vf.getVmId() != null) {
            return false;
        }

        return isNetworkDeviceFree(vf);
    }

    private boolean isNetworkDeviceFree(HostDevice pciDevice) {
        // Check that there is no macvtap device on top of the VM-
        // nics with macvtap attached are not reported via the getVdsCaps
        VdsNetworkInterface vfNic = getNicByPciDevice(pciDevice);

        return vfNic != null && !isNetworkAttached(vfNic) && !isVlanDeviceAttached(vfNic) && !vfNic.isPartOfBond();
    }

    @Override
    public HostDevice getFreeVf(VdsNetworkInterface nic, List<String> excludeVfs) {
        return getVf(nic, true, excludeVfs);
    }

    private HostDevice getVf(VdsNetworkInterface nic, final boolean shouldBeFree, final List<String> excludeVfs) {
        List<HostDevice> deviceList = getDevicesByHostId(nic.getVdsId());
        HostDevice pciDevice = getPciDeviceByNic(nic, deviceList);

        if (pciDevice == null) {
            throw new NullPointerException("nic doesn't have a pci device");
        }

        if (!isSriovDevice(pciDevice)) {
            throw new UnsupportedOperationException("'getVf' method should be called only for 'sriov' nics");
        }

        List<HostDevice> vfs = getVfs(pciDevice, deviceList);

        return vfs.stream()
                .filter(vf -> isVfFree(vf) == shouldBeFree && (excludeVfs == null || !excludeVfs.contains(vf.getDeviceName())))
                .findFirst()
                .orElse(null);
    }

    private HostDevice getVf(VdsNetworkInterface nic, final boolean shouldBeFree) {
        return getVf(nic, shouldBeFree, null);
    }

    private boolean isNetworkAttached(VdsNetworkInterface vfNic) {
        return vfNic.getNetworkName() != null;
    }

    boolean isVlanDeviceAttached(VdsNetworkInterface vfNic) {
        return NetworkUtils.interfaceHasVlan(vfNic, interfaceDao.getAllInterfacesForVds(vfNic.getVdsId()));
    }

    @Override
    public String getPciDeviceNameByNic(VdsNetworkInterface nic) {
        return getPciDeviceByNic(nic, getDevicesByHostId(nic.getVdsId())).getDeviceName();
    }

    @Override
    public void setVmIdOnVfs(Guid hostId, Guid vmId, final Set<String> vfsNames) {
        List<HostDevice> hostDevices = hostDeviceDao.getHostDevicesByHostId(hostId);

        List<HostDevice> vfs = hostDevices.stream()
                .filter(device -> vfsNames.contains(device.getDeviceName()) && isVf(device))
                .collect(Collectors.toList());

        if (vmId != null) {
            HostDevice alreadyTakenVf = vfs.stream().filter(vf -> vf.getVmId() != null).findFirst().orElse(null);

            if (alreadyTakenVf != null) {
                throw new IllegalStateException(
                        String.format("VF %s is already taken by VM %s",
                                alreadyTakenVf.getName(),
                                alreadyTakenVf.getVmId()));
            }
        }
        setVmIdOnVfsDevices(vmId, new HashSet<>(vfs));
    }

    private void setVmIdOnVfsDevices(Guid vmId, Set<HostDevice> vfs) {
        for (HostDevice vf : vfs) {
            hostDeviceDao.setVmIdOnHostDevice(vf.getId(), vmId);
        }
    }

    @Override
    public Guid removeVmIdFromVfs(final Guid vmId) {
        List<HostDevice> hostDevices = hostDeviceDao.getAll();

        List<HostDevice> vfsUsedByVm = hostDevices.stream()
                .filter(device -> vmId.equals(device.getVmId()) && isVf(device)).collect(Collectors.toList());

        Guid hostId = vfsUsedByVm.isEmpty() ? null : vfsUsedByVm.get(0).getHostId();
        if (hostId != null) {
            setVmIdOnVfsDevices(null, new HashSet<>(vfsUsedByVm));
        }

        return hostId;
    }

    @Override
    public Map<Guid, Guid> getVfMap(final Guid hostId) {
        final List<VdsNetworkInterface> hostNics = interfaceDao.getAllInterfacesForVds(hostId);
        final List<HostDevice> hostDevices = hostDeviceDao.getHostDevicesByHostId(hostId);
        final Map<String, HostDevice> hostDevicesByName = Entities.entitiesByName(hostDevices);

        return hostNics.stream()
                .filter(new VfNicPredicate(hostDevices, hostDevicesByName))
                .collect(Collectors.toMap(VdsNetworkInterface::getId,
                        new VfNicToPfNicMapper(hostDevices, hostDevicesByName, hostNics)));
    }

    private boolean isVf(HostDevice device) {
        return StringUtils.isNotBlank(device.getParentPhysicalFunction());
    }

    private class VfNicToPfNicMapper implements Function<VdsNetworkInterface, Guid> {
        private final List<HostDevice> hostDevices;
        private final Map<String, HostDevice> hostDevicesByName;
        private final List<VdsNetworkInterface> hostNics;

        public VfNicToPfNicMapper(List<HostDevice> hostDevices,
                Map<String, HostDevice> hostDevicesByName,
                List<VdsNetworkInterface> hostNics) {
            this.hostDevices = hostDevices;
            this.hostDevicesByName = hostDevicesByName;
            this.hostNics = hostNics;
        }

        @Override
        public Guid apply(VdsNetworkInterface nic) {
            final HostDevice vfPciDevice =
                    getPciDeviceByNic(nic, hostDevices, hostDevicesByName);
            final HostDevice pfPciDevice = hostDevicesByName.get(vfPciDevice.getParentPhysicalFunction());
            final VdsNetworkInterface pfNic = getNicByPciDevice(pfPciDevice, hostDevices, hostNics);

            return pfNic == null ? null : pfNic.getId();
        }
    }

    private class VfNicPredicate implements Predicate<VdsNetworkInterface> {
        private final List<HostDevice> hostDevices;
        private final Map<String, HostDevice> hostDevicesByName;

        public VfNicPredicate(List<HostDevice> hostDevices, Map<String, HostDevice> hostDevicesByName) {
            this.hostDevices = hostDevices;
            this.hostDevicesByName = hostDevicesByName;
        }

        @Override
        public boolean test(VdsNetworkInterface nic) {
            if (nic.isBond() || NetworkCommonUtils.isVlan(nic)) {
                return false;
            }
            try {
                final HostDevice nicPciDevice =
                        getPciDeviceByNic(nic, hostDevices, hostDevicesByName);
                return isVf(nicPciDevice);
            } catch (Exception e) {
                return false;
            }
        }
    }

}
