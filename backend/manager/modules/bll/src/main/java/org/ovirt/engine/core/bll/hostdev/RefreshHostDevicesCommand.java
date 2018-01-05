package org.ovirt.engine.core.bll.hostdev;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.HostLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RefreshHostInfoCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

@NonTransactiveCommandAttribute
public class RefreshHostDevicesCommand<T extends VdsActionParameters> extends RefreshHostInfoCommandBase<T> {

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private HostDeviceDao hostDeviceDao;

    @Inject
    private HostLocking hostLocking;

    @Inject
    private HostNicVfsConfigDao hostNicVfsConfigDao;

    @Inject
    private NetworkDeviceHelper networkDeviceHelper;

    @Inject
    private VmDeviceDao vmDeviceDao;

    @Inject
    private InterfaceDao interfaceDao;

    private Map<String, HostDevice> fetchedMap;
    private Map<String, List<VmDevice>> attachedVmDevicesMap;

    public RefreshHostDevicesCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(VDSCommandType.HostDevListByCaps, new VdsIdAndVdsVDSCommandParametersBase(getVds()));

        if (!vdsReturnValue.getSucceeded()) {
            return;
        }

        List<HostDevice> fetchedDevices = (List<HostDevice>) vdsReturnValue.getReturnValue();
        List<HostDevice> oldDevices = hostDeviceDao.getHostDevicesByHostId(getVdsId());

        Map<String, HostDevice> oldMap = Entities.entitiesByName(oldDevices);
        fetchedMap = filterOrphanedDevices(Entities.entitiesByName(fetchedDevices));

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

                if (getAttachedVmDevicesMap().containsKey(deviceName)) {
                    List<VmDevice> vmDevices = getAttachedVmDevicesMap().get(deviceName);
                    for (VmDevice vmDevice : vmDevices) {
                        log.warn("Removing VM[{}]'s hostDevice[{}] because it no longer exists on host {}",
                                vmDevice.getVmId(), deviceName, getVds());
                        removedVmDevices.add(vmDevice);
                    }
                }
            }
        }

        try {
            hostLocking.acquireHostDevicesLock(getVdsId());
            TransactionSupport.executeInNewTransaction(() -> {

                hostDeviceDao.saveAllInBatch(newDevices);
                hostDeviceDao.updateAllInBatch(changedDevices);
                hostDeviceDao.removeAllInBatch(removedDevices);

                handleHostNicVfsConfigUpdate();

                vmDeviceDao.removeAllInBatch(removedVmDevices);

                return null;
            });
        } finally {
            hostLocking.releaseHostDevicesLock(getVdsId());
        }

        setSucceeded(true);
    }

    /**
     * Filters out devices which may be orphaned (their parent is no longer included in device list)
     * or otherwise invalid (parent is null or empty).
     *
     * This is done transitively by using DFS started in the root device (computer) and
     * adding only reachable devices via the "parent device" relationship.
     */
    static Map<String, HostDevice> filterOrphanedDevices(Map<String, HostDevice> fetchedDevicesMap) {
        if (!fetchedDevicesMap.containsKey(VdsProperties.ROOT_HOST_DEVICE)) {
            // if there is no root, nothing can be reachable from root
            return Collections.emptyMap();
        }

        Map<String, List<String>> childrenDeviceMap =
                fetchedDevicesMap.entrySet().stream().collect(Collectors.groupingBy(
                        e -> e.getValue().getParentDeviceName(),
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

        Stack<String> toTraverse = new Stack<>();
        toTraverse.push(VdsProperties.ROOT_HOST_DEVICE);

        Map<String, HostDevice> result = new HashMap<>();
        while (!toTraverse.empty()) {
            String deviceName = toTraverse.pop();
            result.put(deviceName, fetchedDevicesMap.get(deviceName));
            if (childrenDeviceMap.containsKey(deviceName)) {
                childrenDeviceMap.get(deviceName).stream()
                        // prevent infinite loop by adding root again as a child of root
                        .filter(child -> !VdsProperties.ROOT_HOST_DEVICE.equals(child))
                        .forEach(toTraverse::push);
            }
        }
        return result;
    }

    /**
     * Returns lazily computed map of device names -> list of vm devices representing device attachments for this host.
     */
    private Map<String, List<VmDevice>> getAttachedVmDevicesMap() {
        if (attachedVmDevicesMap == null) {
            List<VmDevice> vmDevices = hostDeviceDao.getVmDevicesAttachedToHost(getVdsId());

            attachedVmDevicesMap = vmDevices.stream().collect(Collectors.groupingBy(VmDevice::getDevice));
        }

        return attachedVmDevicesMap;
    }

    private void handleHostNicVfsConfigUpdate() {
        removeInvalidHostNicVfsConfigsFromDb();
        addMissingHostNicVfsConfigsToDb();
    }

    private void removeInvalidHostNicVfsConfigsFromDb() {
        final List<HostNicVfsConfig> hostNicVfsConfigsToRemove = new ArrayList<>();

        List<HostNicVfsConfig> hostNicVfsConfigs = hostNicVfsConfigDao.getAllVfsConfigByHostId(getVdsId());

        for (HostNicVfsConfig hostNicVfsConfig : hostNicVfsConfigs) {

            VdsNetworkInterface nic = interfaceDao.get(hostNicVfsConfig.getNicId());

            HostDevice pciDevice = null;
            if (nic != null) {
                String pciDeviceName = networkDeviceHelper.getPciDeviceNameByNic(nic);
                pciDevice = fetchedMap.get(pciDeviceName);
            }

            if (nic == null || pciDevice == null || !networkDeviceHelper.isSriovDevice(pciDevice)) {
                addToListIfNotNull(hostNicVfsConfig, hostNicVfsConfigsToRemove);
            }
        }

        if (!hostNicVfsConfigsToRemove.isEmpty()) {
            hostNicVfsConfigDao.removeAllInBatch(hostNicVfsConfigsToRemove);
        }
    }

    private void addMissingHostNicVfsConfigsToDb() {
        final List<HostNicVfsConfig> hostNicVfsConfigsToAdd = new ArrayList<>();
        for (HostDevice device : fetchedMap.values()) {
            if (networkDeviceHelper.isSriovDevice(device)) {
                addToListIfNotNull(createHostNicVfsConfigToAddIfNotExist(device), hostNicVfsConfigsToAdd);
            }
        }

        if (!hostNicVfsConfigsToAdd.isEmpty()) {
            hostNicVfsConfigDao.saveAllInBatch(hostNicVfsConfigsToAdd);
        }
    }

    private <E> void addToListIfNotNull(E element, Collection<E> collection) {
        if (element != null) {
            collection.add(element);
        }
    }

    private HostNicVfsConfig createHostNicVfsConfigToAddIfNotExist(HostDevice device) {
        VdsNetworkInterface nic = networkDeviceHelper.getNicByPciDevice(device, fetchedMap.values());

        if (nic == null) {
            return null;
        }

        HostNicVfsConfig existingHostNicVfsConfig = hostNicVfsConfigDao.getByNicId(nic.getId());

        if (existingHostNicVfsConfig != null) {
            return null;
        }

        return new HostNicVfsConfig(Guid.newGuid(), nic.getId(), true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REFRESH);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST_DEVICES);
    }
}
