package org.ovirt.engine.core.bll.network.host;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Mapper;
import org.ovirt.engine.core.utils.linq.Predicate;

@Singleton
class NetworkDeviceHelperImpl implements NetworkDeviceHelper {

    private final InterfaceDao interfaceDao;
    private final HostDeviceDao hostDeviceDao;
    private final HostNicVfsConfigDao hostNicVfsConfigDao;
    private final VdsDao vdsDao;

    @Inject
    NetworkDeviceHelperImpl(InterfaceDao interfaceDao,
            HostDeviceDao hostDeviceDao,
            HostNicVfsConfigDao hostNicVfsConfigDao,
            VdsDao vdsDao) {
        Objects.requireNonNull(interfaceDao, "interfaceDao cannot be null");
        Objects.requireNonNull(hostDeviceDao, "hostDeviceDao cannot be null");
        Objects.requireNonNull(hostNicVfsConfigDao, "hostNicVfsConfigDao cannot be null");
        Objects.requireNonNull(vdsDao, "vdsDao cannot be null");

        this.interfaceDao = interfaceDao;
        this.hostDeviceDao = hostDeviceDao;
        this.hostNicVfsConfigDao = hostNicVfsConfigDao;
        this.vdsDao = vdsDao;
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
        final HostDevice netDevice = getFirstChildDevice(pciDevice, devices);

        if (netDevice == null || !isNetworkDevice(netDevice)) {
            return null;
        }

        final Collection<VdsNetworkInterface> hostInterfaces =
                hostNics == null ? interfaceDao.getAllInterfacesForVds(netDevice.getHostId()) : hostNics;

        return LinqUtils.firstOrNull(hostInterfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface iface) {
                return iface.getName().equals(netDevice.getNetworkInterfaceName());
            }
        });
    }

    private HostDevice getFirstChildDevice(final HostDevice pciDevice, final Collection<HostDevice> devices) {
        Collection<HostDevice> hostDevices = devices == null ? getDevicesByHostId(pciDevice.getHostId()) : devices;
        return LinqUtils.firstOrNull(hostDevices, new Predicate<HostDevice>() {

            @Override
            public boolean eval(HostDevice device) {
                return pciDevice.getDeviceName().equals(device.getParentDeviceName());
            }
        });
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
    }

    private HostDevice getPciDeviceByNic(final VdsNetworkInterface nic, List<HostDevice> deviceList) {
        return getPciDeviceByNic(nic, deviceList, Entities.entitiesByName(deviceList));
    }

    private HostDevice getPciDeviceByNic(final VdsNetworkInterface nic,
            List<HostDevice> deviceList,
            Map<String, HostDevice> devicesByName) {
        final String nicName = nic.getName();
        final HostDevice netDevice = LinqUtils.firstOrNull(deviceList, new Predicate<HostDevice>() {

            @Override
            public boolean eval(HostDevice device) {
                return nicName.equals(device.getNetworkInterfaceName());
            }
        });

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
        return LinqUtils.filter(deviceList, new Predicate<HostDevice>() {

            @Override
            public boolean eval(HostDevice device) {
                return pciDevice.getDeviceName().equals(device.getParentPhysicalFunction());
            }
        });
    }

    @Override
    public boolean areAllVfsFree(VdsNetworkInterface nic) {
        HostDevice nonFreeVf = getVf(nic, false);

        return nonFreeVf == null;
    }

    @Override
    public boolean isDeviceNetworkFree(HostDevice hostDevice) {
        HostDevice firstChild = getFirstChildDevice(hostDevice, null);
        if (firstChild == null || !isNetworkDevice(firstChild)) {
            return true;
        }

        return isNetworkDeviceFree(firstChild);
    }

    private boolean isVfFree(HostDevice vf) {
        // Check if the VF is attached directly to a VM
        if (vf.getVmId() != null) {
            return false;
        }

        return isNetworkDeviceFree(vf);
    }

    private boolean isNetworkDeviceFree(HostDevice networkDevice) {
        // Check that there is no macvtap device on top of the VM-
        // nics with macvtap attached are not reported via the getVdsCaps
        VdsNetworkInterface vfNic = getNicByPciDevice(networkDevice);

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

        HostDevice vf = LinqUtils.firstOrNull(vfs, new Predicate<HostDevice>() {

            @Override
            public boolean eval(HostDevice vf) {
                return isVfFree(vf) == shouldBeFree && (excludeVfs == null || !excludeVfs.contains(vf.getDeviceName()));
            }
        });

       return vf;
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

        List<HostDevice> vfs = LinqUtils.filter(hostDevices, new Predicate<HostDevice>() {

            @Override
            public boolean eval(HostDevice device) {
                return vfsNames.contains(device.getDeviceName()) && isVf(device);
            }
        });

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

        List<HostDevice> vfsUsedByVm = LinqUtils.filter(hostDevices, new Predicate<HostDevice>() {

            @Override
            public boolean eval(HostDevice device) {
                return vmId.equals(device.getVmId()) && isVf(device);
            }
        });

        Guid hostId = vfsUsedByVm.isEmpty() ? null : vfsUsedByVm.get(0).getHostId();
        if (hostId != null) {
            setVmIdOnVfsDevices(null, new HashSet<>(vfsUsedByVm));
        }

        return hostId;
    }

    @Override
    public Map<Guid, Guid> getVfMap(final Guid hostId) {
        final VDS host = vdsDao.get(hostId);
        if (!FeatureSupported.sriov(host.getVdsGroupCompatibilityVersion())) {
            return Collections.emptyMap();
        }

        final List<VdsNetworkInterface> hostNics = interfaceDao.getAllInterfacesForVds(hostId);
        final List<HostDevice> hostDevices = hostDeviceDao.getHostDevicesByHostId(hostId);
        final Map<String, HostDevice> hostDevicesByName = Entities.entitiesByName(hostDevices);

        final List<VdsNetworkInterface> vfNics = LinqUtils.filter(hostNics,
                new VfNicPredicate(hostDevices, hostDevicesByName));
        final Map<Guid, Guid> result =
                LinqUtils.toMap(vfNics, new VfNicToPfNicMapper(hostDevices, hostDevicesByName, hostNics));

        return result;
    }

    private boolean isVf(HostDevice device) {
        return StringUtils.isNotBlank(device.getParentPhysicalFunction());
    }

    private class VfNicToPfNicMapper implements Mapper<VdsNetworkInterface, Guid, Guid> {
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
        public Guid createKey(VdsNetworkInterface nic) {
            return nic.getId();
        }

        @Override
        public Guid createValue(VdsNetworkInterface nic) {
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
        public boolean eval(VdsNetworkInterface nic) {
            if (nic.isBond() || NetworkUtils.isVlan(nic)) {
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
