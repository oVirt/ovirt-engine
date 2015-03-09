package org.ovirt.engine.core.bll.hostdev;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.VmDeviceDAO;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractVmHostDevicesCommand<P extends VmHostDevicesParameters> extends VmCommand<P> {

    private static final String CAPABILITY_PCI = "pci";

    @Inject
    private VmDeviceDAO vmDeviceDao;

    @Inject
    private HostDeviceDao hostDeviceDao;

    private List<HostDevice> hostDevices;

    public AbstractVmHostDevicesCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (getParameters().getDeviceNames() == null || getParameters().getDeviceNames().isEmpty()) {
            failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DEVICE_MUST_BE_SPECIFIED);
        }

        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        // hot(un)plug not supported (yet)
        if (getVm().getStatus() != VMStatus.Down) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        }

        if (getVm().getDedicatedVmForVds() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
        }

        if (getHostDevices() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_HOST_DEVICE_NOT_FOUND);
        }

        return true;
    }

    protected VmDeviceDAO getVmDeviceDao() {
        return vmDeviceDao;
    }

    private List<HostDevice> getHostDevices() {
        if (hostDevices == null) {
            hostDevices = new ArrayList<>();
            for (String deviceName : getParameters().getDeviceNames()) {
                HostDevice hostDevice = fetchHostDevice(deviceName);
                if (hostDevice == null) {
                    return null;
                }
                hostDevices.add(hostDevice);
            }
        }
        return hostDevices;
    }

    protected Set<HostDevice> getAffectedHostDevices() {
        Set<HostDevice> affectedDevices = new HashSet<>();
        for (HostDevice hostDevice : getHostDevices()) {
            affectedDevices.addAll(getDeviceAtomicGroup(hostDevice));
        }
        return affectedDevices;
    }

    private Collection<HostDevice> getDeviceAtomicGroup(HostDevice hostDevice) {
        // iommu group restriction only applicable to 'pci' devices
        if (!CAPABILITY_PCI.equals(hostDevice.getCapability()) || hostDevice.getIommuGroup() == null) {
            return Collections.singleton(hostDevice);
        }

        return hostDeviceDao.getHostDevicesByHostIdAndIommuGroup(getVm().getDedicatedVmForVds(),
                hostDevice.getIommuGroup());
    }

    private HostDevice fetchHostDevice(String deviceName) {
        return hostDeviceDao.getHostDeviceByHostIdAndDeviceName(getVm().getDedicatedVmForVds(), deviceName);
    }

    protected Map<String, VmDevice> getExistingVmHostDevicesByName() {
        List<VmDevice> existingDevices = vmDeviceDao.getVmDeviceByVmIdAndType(getVmId(), VmDeviceGeneralType.HOSTDEV);
        return Entities.vmDevicesByDevice(existingDevices);
    }
}
