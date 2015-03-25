package org.ovirt.engine.core.bll.hostdev;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RefreshHostInfoCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.host.HostNicVfsConfigHelper;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@NonTransactiveCommandAttribute
public class RefreshHostDevicesCommand<T extends VdsActionParameters> extends RefreshHostInfoCommandBase<T> {

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private DbFacade dbFacade;

    @Inject
    private HostNicVfsConfigDao hostNicVfsConfigDao;

    @Inject
    private HostNicVfsConfigHelper hostNicVfsConfigHelper;

    public RefreshHostDevicesCommand(T parameters) {
        super(parameters);
    }

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
        List<HostDevice> oldDevices = dbFacade.getHostDeviceDao().getHostDevicesByHostId(getVdsId());

        Map<String, HostDevice> fetchedMap = groupDevicesByName(fetchedDevices);
        final Map<String, HostDevice> oldMap = groupDevicesByName(oldDevices);

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
        for (Map.Entry<String, HostDevice> entry : oldMap.entrySet()) {
            if (!fetchedMap.containsKey(entry.getKey())) {
                removedDevices.add(entry.getValue());
            }
        }

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                dbFacade.getHostDeviceDao().saveAllInBatch(newDevices);
                dbFacade.getHostDeviceDao().updateAllInBatch(changedDevices);

                handleHostNicVfsConfigUpdate(oldMap, newDevices, changedDevices, removedDevices);

                dbFacade.getHostDeviceDao().removeAllInBatch(removedDevices);

                return null;
            }
        });

        setSucceeded(true);
    }

    private void handleHostNicVfsConfigUpdate(final Map<String, HostDevice> oldMap,
            final List<HostDevice> newDevices,
            final List<HostDevice> changedDevices,
            final List<HostDevice> removedDevices) {
        final List<HostNicVfsConfig> newHostNicVfsConfigs = new ArrayList<>();
        final List<HostNicVfsConfig> removedHostNicVfsConfigs = new ArrayList<>();

        for (HostDevice device : newDevices) {
            if (hostNicVfsConfigHelper.isSriovDevice(device)) {
                addToListIfNotNull(getHostNicVfsConfigToAdd(device), newHostNicVfsConfigs);
            }
        }

        for (HostDevice device : changedDevices) {
            HostDevice oldDevice = oldMap.get(device.getDeviceName());

            if (!hostNicVfsConfigHelper.isSriovDevice(oldDevice)
                    && hostNicVfsConfigHelper.isSriovDevice(device)) {
                addToListIfNotNull(getHostNicVfsConfigToAdd(device), newHostNicVfsConfigs);
            }

            if (hostNicVfsConfigHelper.isSriovDevice(oldDevice)
                    && !hostNicVfsConfigHelper.isSriovDevice(device)) {
                addToListIfNotNull(getHostNicVfsConfigToRemove(device), removedHostNicVfsConfigs);
            }
        }

        for (HostDevice device : removedDevices) {
            if (hostNicVfsConfigHelper.isSriovDevice(device)) {
                addToListIfNotNull(getHostNicVfsConfigToRemove(device), removedHostNicVfsConfigs);
            }
        }

        if (!newHostNicVfsConfigs.isEmpty()) {
            hostNicVfsConfigDao.saveAllInBatch(newHostNicVfsConfigs);
        }

        if (!removedHostNicVfsConfigs.isEmpty()) {
            hostNicVfsConfigDao.removeAllInBatch(removedHostNicVfsConfigs);
        }
    }

    private <E> void addToListIfNotNull(E element, Collection<E> collection) {
        if (element != null) {
            collection.add(element);
        }
    }

    private HostNicVfsConfig getHostNicVfsConfigToAdd(HostDevice device) {
        VdsNetworkInterface nic = hostNicVfsConfigHelper.getNicByPciDevice(device);

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
        VdsNetworkInterface nic = hostNicVfsConfigHelper.getNicByPciDevice(device);
        if (nic == null) {
            return null;
        }

        return hostNicVfsConfigDao.getByNicId(nic.getId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REFRESH);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST_DEVICES);
    }

    private static Map<String, HostDevice> groupDevicesByName(List<HostDevice> devices) {
        Map<String, HostDevice> map = new HashMap<>();
        for (HostDevice device : devices) {
            map.put(device.getDeviceName(), device);
        }
        return map;
    }
}
