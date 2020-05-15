package org.ovirt.engine.core.bll.hostdev;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;

public abstract class AbstractVmHostDevicesCommand<P extends VmHostDevicesParameters> extends VmCommand<P> {

    @Inject
    private HostDeviceDao hostDeviceDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;

    private List<HostDevice> hostDevices;

    /**
     * Contains device names that are explicitly passed in the command parameters
     */
    private Set<String> primaryDeviceNames;

    public AbstractVmHostDevicesCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        primaryDeviceNames = new HashSet<>(parameters.getDeviceNames());
    }

    @Override
    protected boolean validate() {
        if (getParameters().getDeviceNames() == null || getParameters().getDeviceNames().isEmpty()) {
            failValidation(EngineMessage.ACTION_TYPE_FAILED_DEVICE_MUST_BE_SPECIFIED);
        }

        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        // hot(un)plug not supported (yet)
        if (getVm().getStatus() != VMStatus.Down) {
            return failVmStatusIllegal();
        }

        if (getVm().getDedicatedVmForVdsList().isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
        }

        if (getVm().getDedicatedVmForVdsList().size() > 1) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_PINNED_TO_MULTIPLE_HOSTS);
        }

        if (getHostDevices() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOST_DEVICE_NOT_FOUND);
        }

        for (HostDevice hostDevice : getAffectedHostDevices()) {
            if (hostDevice.getCapability().equals("nvdimm")) {
                if (vmInfoBuildUtils.getVmNumaNodes(getVm()).isEmpty()) {
                    return failValidation(EngineMessage.VM_NVDIMM_NUMA_UNAVAILABLE);
                }
                break;
            }
        }

        return true;
    }

    protected Set<String> getPrimaryDeviceNames() {
        return primaryDeviceNames;
    }

    protected List<HostDevice> getHostDevices() {
        if (hostDevices == null) {
            hostDevices = getParameters().getDeviceNames()
                    .stream()
                    .map(this::fetchHostDevice)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (hostDevices.size() != getParameters().getDeviceNames().size()) {
                // didn't find one or more of the specified devices
                hostDevices = null;
            }
        }
        return hostDevices;
    }

    protected Set<HostDevice> getAffectedHostDevices() {
        return getHostDevices().stream()
                .map(this::getDeviceAtomicGroup)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Collection<HostDevice> getDeviceAtomicGroup(HostDevice hostDevice) {
        if (!hasIommu(hostDevice)) {
            return Collections.singleton(hostDevice);
        }
        // only single dedicated host allowed
        return hostDeviceDao.getHostDevicesByHostIdAndIommuGroup(getVm().getDedicatedVmForVdsList().get(0),
                hostDevice.getIommuGroup());
    }

    protected boolean hasIommu(HostDevice hostDevice) {
        // iommu group restriction only applicable to 'pci' devices
        return hostDevice.isPci() && hostDevice.getIommuGroup() != null;
    }

    private HostDevice fetchHostDevice(String deviceName) {
        // single dedicated host allowed.
        return hostDeviceDao.getHostDeviceByHostIdAndDeviceName(getVm().getDedicatedVmForVdsList().get(0), deviceName);
    }

    protected Map<String, VmHostDevice> getExistingVmHostDevicesByName() {
        List<VmHostDevice> devices = vmDeviceDao.getVmDeviceByVmIdAndType(getVmId(), VmDeviceGeneralType.HOSTDEV)
                .stream()
                .map(VmHostDevice::new)
                .collect(Collectors.toList());
        return getVmDeviceUtils().vmDevicesByDevice(devices);
    }
}
