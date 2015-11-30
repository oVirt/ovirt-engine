package org.ovirt.engine.core.bll.hostdev;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RefreshHostInfoCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@NonTransactiveCommandAttribute
public class RefreshHostDevicesCommand<T extends VdsActionParameters> extends RefreshHostInfoCommandBase<T> {

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private HostDeviceDao hostDeviceDao;

    @Inject
    private HostDeviceManager hostDeviceManager;

    @Inject
    private HostNicVfsConfigDao hostNicVfsConfigDao;

    @Inject
    private NetworkDeviceHelper networkDeviceHelper;

    private Map<String, HostDevice> oldMap;

    private Map<String, HostDevice> fetchedMap;

    public RefreshHostDevicesCommand(T parameters) {
        super(parameters);
    }

    public RefreshHostDevicesCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        if (!FeatureSupported.hostDevicePassthrough(getVds().getVdsGroupCompatibilityVersion())) {
            // Do nothing if the host doesn't support the HostDevListByCaps verb
            setSucceeded(true);
            return;
        }

        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(VDSCommandType.HostDevListByCaps, new VdsIdAndVdsVDSCommandParametersBase(getVds()));

        if (!vdsReturnValue.getSucceeded()) {
            return;
        }

        List<HostDevice> fetchedDevices = (List<HostDevice>) vdsReturnValue.getReturnValue();
        List<HostDevice> oldDevices = hostDeviceDao.getHostDevicesByHostId(getVdsId());
        List<VmDevice> vmDevices = getVmDeviceDao().getVmDeviceByType(VmDeviceGeneralType.HOSTDEV);

        fetchedMap = Entities.entitiesByName(fetchedDevices);
        oldMap = Entities.entitiesByName(oldDevices);
        Map<String, VmDevice> vmDeviceMap = Entities.vmDevicesByDevice(vmDevices);

        final List<HostDevice> newDevices = new ArrayList<>();
        final List<HostDevice> changedDevices = new ArrayList<>();

        for (Map.Entry<String, HostDevice> entry : fetchedMap.entrySet()) {
            HostDevice device = entry.getValue();
            if (oldMap.containsKey(entry.getKey())) {
                if (!oldMap.get(entry.getKey()).equals(device)) {
                    changedDevices.add(device);
                }
            } else {
                newDevices.add(device);
            }
        }

        final List<HostDevice> removedDevices = new ArrayList<>();
        final List<VmDevice> removedVmDevices = new ArrayList<>();
        for (Map.Entry<String, HostDevice> entry : oldMap.entrySet()) {
            final String deviceName = entry.getKey();
            if (!fetchedMap.containsKey(deviceName)) {
                removedDevices.add(entry.getValue());

                if (vmDeviceMap.containsKey(deviceName)) {
                    VmDevice vmDevice = vmDeviceMap.get(deviceName);
                    log.warn("Removing VM[{}]'s hostDevice[{}] because it no longer exists on host {}",
                            vmDevice.getVmId(), deviceName, getVds());
                    removedVmDevices.add(vmDevice);
                }
            }
        }

        try {
            hostDeviceManager.acquireHostDevicesLock(getVdsId());
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {

                    hostDeviceDao.saveAllInBatch(newDevices);
                    hostDeviceDao.updateAllInBatch(changedDevices);
                    hostDeviceDao.removeAllInBatch(removedDevices);

                    handleHostNicVfsConfigUpdate(newDevices, changedDevices, removedDevices);

                    getVmDeviceDao().removeAllInBatch(removedVmDevices);

                    return null;
                }
            });
        } finally {
            hostDeviceManager.releaseHostDevicesLock(getVdsId());
        }

        setSucceeded(true);
    }

    private void handleHostNicVfsConfigUpdate(final List<HostDevice> newDevices,
            final List<HostDevice> changedDevices,
            final List<HostDevice> removedDevices) {
        final List<HostNicVfsConfig> newHostNicVfsConfigs = new ArrayList<>();
        final List<HostNicVfsConfig> removedHostNicVfsConfigs = new ArrayList<>();

        for (HostDevice device : newDevices) {
            if (networkDeviceHelper.isSriovDevice(device)) {
                addToListIfNotNull(getHostNicVfsConfigToAdd(device), newHostNicVfsConfigs);
            }
        }

        for (HostDevice device : changedDevices) {
            HostDevice oldDevice = oldMap.get(device.getDeviceName());

            if (!networkDeviceHelper.isSriovDevice(oldDevice)
                    && networkDeviceHelper.isSriovDevice(device)) {
                addToListIfNotNull(getHostNicVfsConfigToAdd(device), newHostNicVfsConfigs);
            }

            if (networkDeviceHelper.isSriovDevice(oldDevice)
                    && !networkDeviceHelper.isSriovDevice(device)) {
                addToListIfNotNull(getHostNicVfsConfigToRemove(device), removedHostNicVfsConfigs);
            }
        }

        for (HostDevice device : removedDevices) {
            if (networkDeviceHelper.isSriovDevice(device)) {
                addToListIfNotNull(getHostNicVfsConfigToRemove(device), removedHostNicVfsConfigs);
            }
        }

        if (!removedHostNicVfsConfigs.isEmpty()) {
            hostNicVfsConfigDao.removeAllInBatch(removedHostNicVfsConfigs);
        }

        if (!newHostNicVfsConfigs.isEmpty()) {
            hostNicVfsConfigDao.saveAllInBatch(newHostNicVfsConfigs);
        }
    }

    private <E> void addToListIfNotNull(E element, Collection<E> collection) {
        if (element != null) {
            collection.add(element);
        }
    }

    private HostNicVfsConfig getHostNicVfsConfigToAdd(HostDevice device) {
        VdsNetworkInterface nic = networkDeviceHelper.getNicByPciDevice(device, fetchedMap.values());

        if (nic == null) {
            return null;
        }

        HostNicVfsConfig hostNicVfsConfig = new HostNicVfsConfig();
        hostNicVfsConfig.setId(Guid.newGuid());
        hostNicVfsConfig.setNicId(nic.getId());
        hostNicVfsConfig.setAllNetworksAllowed(true);

        return hostNicVfsConfig;
    }

    private HostNicVfsConfig getHostNicVfsConfigToRemove(HostDevice device) {
        VdsNetworkInterface nic = networkDeviceHelper.getNicByPciDevice(device, oldMap.values());
        if (nic == null) {
            return null;
        }

        return hostNicVfsConfigDao.getByNicId(nic.getId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__REFRESH);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__HOST_DEVICES);
    }
}
