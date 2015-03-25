package org.ovirt.engine.core.bll.network.host;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@Singleton
class HostNicVfsConfigHelperImpl implements HostNicVfsConfigHelper {

    private final InterfaceDao interfaceDao;
    private final HostDeviceDao hostDeviceDao;
    private final HostNicVfsConfigDao hostNicVfsConfigDao;

    @Inject
    HostNicVfsConfigHelperImpl(InterfaceDao interfaceDao,
            HostDeviceDao hostDeviceDao,
            HostNicVfsConfigDao hostNicVfsConfigDao) {
        this.interfaceDao = interfaceDao;
        this.hostDeviceDao = hostDeviceDao;
        this.hostNicVfsConfigDao = hostNicVfsConfigDao;
    }

    @Override
    public VdsNetworkInterface getNicByPciDevice(final HostDevice pciDevice) {
        final HostDevice netDevice = getNetDeviceByPciDevice(pciDevice);

        if (netDevice == null || !isNetworkDevice(netDevice)) {
            return null;
        }

        List<VdsNetworkInterface> hostInterfaces =
                interfaceDao.getAllInterfacesForVds(netDevice.getHostId());

        return LinqUtils.firstOrNull(hostInterfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface iface) {
                return iface.getName().equals(netDevice.getNetworkInterfaceName());
            }
        });
    }

    private HostDevice getNetDeviceByPciDevice(final HostDevice pciDevice) {
        return LinqUtils.firstOrNull(getDevicesByHostId(pciDevice.getHostId()), new Predicate<HostDevice>() {

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
        hostNicVfsConfig.setNumOfVfs(getNumOfVfs(nic.getName(), pciDevice, deviceList));
    }

    private HostDevice getPciDeviceByNic(final VdsNetworkInterface nic, List<HostDevice> deviceList) {
        HostDevice netDevice = LinqUtils.firstOrNull(deviceList, new Predicate<HostDevice>() {

            @Override
            public boolean eval(HostDevice device) {
                return nic.getName().equals(device.getNetworkInterfaceName());
            }
        });

        if (netDevice != null) {
            return hostDeviceDao.getHostDeviceByHostIdAndDeviceName(nic.getVdsId(),
                    netDevice.getParentDeviceName());
        }

        return null;
    }

    private int getMaxNumOfVfs(HostDevice pciDevice) {
        return pciDevice.getTotalVirtualFunctions();
    }

    private int getNumOfVfs(final String nicName, HostDevice pciDevice, List<HostDevice> deviceList) {
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

    private boolean isVfFree(HostDevice vf) {
        // Check if the VF is attached directly to a VM
        if (vf.getVmId() != null) {
            return false;
        }

        // Check that there is no macvtap device on top of the VM-
        // nics with macvtap attached are not reported via the getVdsCaps
        VdsNetworkInterface vfNic = getNicByPciDevice(vf);

        return vfNic != null && !isNetworkAttached(vfNic) && !isVlanDeviceAttached(vfNic);
    }

    @Override
    public HostDevice getFreeVf(VdsNetworkInterface nic) {
        return getVf(nic, true);
    }

    private HostDevice getVf(VdsNetworkInterface nic, final boolean shouldBeFree) {
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
                return isVfFree(vf) == shouldBeFree;
            }
        });

        return vf;
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
                return vfsNames.contains(device.getDeviceName());
            }
        });

        for (HostDevice vf : vfs) {
            vf.setVmId(vmId);
        }

        hostDeviceDao.updateAllInBatch(vfs);
    }
}
