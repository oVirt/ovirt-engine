package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.OnTimerMethodAnnotation;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StorageDeviceSyncJob extends GlusterJob {
    private static final Logger log = LoggerFactory.getLogger(StorageDeviceSyncJob.class);

    @Override
    public Collection<GlusterJobSchedulingDetails> getSchedulingDetails() {
        return Collections.singleton(new GlusterJobSchedulingDetails(
                "gluster_storage_device_pool_event", getRefreshRate(ConfigValues.GlusterRefreshRateStorageDevices)));
    }

    public void init() {
        log.info("Gluster Storage Device monitoring has been initialized");
    }

    @OnTimerMethodAnnotation("gluster_storage_device_pool_event")
    public void refreshStorageDevices() {
        // get all clusters
        List<Cluster> clusters = clusterDao.getAll();
        // for every cluster that supports disk provisioning
        for (Cluster cluster : clusters) {
            if (supportsGlusterDiskProvisioning(cluster)) {
                refreshStorageDevicesFromServers(glusterUtil.getAllUpServers(cluster.getId()));
            }
        }
    }

    private void refreshStorageDevicesFromServers(List<VDS> upServers) {
        List<Callable<Pair<VDS, List<StorageDevice>>>> storageDevicesListCalls = new ArrayList<>();

        for (final VDS server : upServers) {
            storageDevicesListCalls.add(() -> {
                List<StorageDevice> storageDevices = getStorageDevicesFromServer(server);
                return new Pair<>(server, storageDevices);
            });
        }

        if (!storageDevicesListCalls.isEmpty()) {
            List<Pair<VDS, List<StorageDevice>>> storageDevices = ThreadPoolUtil.invokeAll(storageDevicesListCalls);
            for (Pair<VDS, List<StorageDevice>> pair : storageDevices) {
                if (pair.getSecond() != null) {
                    updateStorageDevices(pair.getFirst(), pair.getSecond());
                }
            }
        }
    }

    private List<StorageDevice> getStorageDevicesFromServer(VDS server) {
        try {
            VDSReturnValue returnValue =
                    runVdsCommand(VDSCommandType.GetStorageDeviceList,
                            new VdsIdVDSCommandParametersBase(server.getId()));
            if (returnValue.getSucceeded()) {
                return (List<StorageDevice>) returnValue.getReturnValue();
            } else {
                log.error("VDS error retriving storage device {}", returnValue.getVdsError().getMessage());
                log.debug("VDS Error", returnValue.getVdsError());
                return null;
            }
        } catch (Exception e) {
            log.error("Exception retriving storage device from vds {}", e.getMessage());
            log.debug("Exception", e);
            return null;
        }

    }

    public void updateStorageDevices(VDS vds, List<StorageDevice> storageDevicesFromVdsm) {
        Set<String> deviceUuidsFromVdsm = new HashSet<>();
        Set<String> deviceNamesFromVdsm = new HashSet<>();

        List<StorageDevice> storageDevicesInDb = storageDeviceDao.getStorageDevicesInHost(vds.getId());
        Map<String, StorageDevice> nameToDeviceMap = new HashMap<>();
        Map<String, StorageDevice> deviceUuidToDeviceMap = new HashMap<>();

        // Make deviceUuid to Device map and deviceName to device map so that we can find the
        // newly added and updated devices without looping over the same list again and again.
        for (StorageDevice storageDevice : storageDevicesInDb) {
            nameToDeviceMap.put(storageDevice.getName(), storageDevice);
            if (storageDevice.getDevUuid() != null && !storageDevice.getDevUuid().isEmpty()) {
                deviceUuidToDeviceMap.put(storageDevice.getDevUuid(), storageDevice);
            }
        }

        List<StorageDevice> storageDevicesToUpdate = new ArrayList<>();
        List<StorageDevice> storageDevicesToDelete = new ArrayList<>();

        for (StorageDevice storageDevice : storageDevicesFromVdsm) {
            // Create deviceName and deviceUuid set to use it while finding the deleted services.
            deviceNamesFromVdsm.add(storageDevice.getName());
            if (storageDevice.getDevUuid() != null) {
                deviceUuidsFromVdsm.add(storageDevice.getDevUuid());
            }
            // If DevUuid is already exits in the DB then its an existing devices
            // Assume device from vdsm doesn't have devUUID, but device name already exists in the DB
            // Following two cases possible:
            // 1. If device in DB doesn't have a devUUID
            // update the device if there is a change from vdsm.
            // 2. If device in DB has devUUID
            // Though name matches, its two different devices. So treat this device as new one.
            // Device in DB will be updated/removed by some other iteration in the loop

            StorageDevice storageDevByDevUuid = deviceUuidToDeviceMap.get(storageDevice.getDevUuid());
            StorageDevice storageDevByName = nameToDeviceMap.get(storageDevice.getName());
            if (storageDevByDevUuid != null) {
                storageDevice.setId(storageDevByDevUuid.getId());
                if (!Objects.equals(storageDevByDevUuid, storageDevice)) {
                    storageDevicesToUpdate.add(storageDevice);
                }
            } else if (storageDevByName != null && StringUtils.isBlank(storageDevByName.getDevUuid())) {
                storageDevice.setId(storageDevByName.getId());
                if (!Objects.equals(storageDevByName, storageDevice)) {
                    storageDevicesToUpdate.add(storageDevice);
                }
            } else {
                storageDevice.setId(Guid.newGuid());
                storageDevice.setVdsId(vds.getId());
                log.debug("detected new storage device '{}' for host '{}'",
                        storageDevice.getName(),
                        vds.getName());
                storageDeviceDao.save(storageDevice);
                logStorageDeviceMessage(AuditLogType.NEW_STORAGE_DEVICE_DETECTED,
                        vds,
                        storageDevice);
            }
        }

        for (StorageDevice storageDevice : storageDevicesInDb) {
            if ((storageDevice.getDevUuid() != null && !deviceUuidsFromVdsm.contains(storageDevice.getDevUuid()))
                    || (storageDevice.getDevUuid() == null && !deviceNamesFromVdsm.contains(storageDevice.getName()))) {
                log.debug("storage device '{}' detected removed for the host '{}'",
                        storageDevice.getName(),
                        vds.getName());
                logStorageDeviceMessage(AuditLogType.STORAGE_DEVICE_REMOVED_FROM_THE_HOST,
                        vds,
                        storageDevice);
                storageDevicesToDelete.add(storageDevice);
            }
        }

        if (!storageDevicesToUpdate.isEmpty()) {
            storageDeviceDao.updateAllInBatch(storageDevicesToUpdate);
        }
        if (!storageDevicesToDelete.isEmpty()) {
            storageDeviceDao.removeAllInBatch(storageDevicesToDelete);
        }

    }

    private void logStorageDeviceMessage(AuditLogType logType, VDS vds, final StorageDevice device) {
        logUtil.logAuditMessage(vds.getClusterId(), vds.getClusterName(), null,
                vds, logType, Collections.singletonMap("storageDevice", device.getName()));
    }

    private boolean supportsGlusterDiskProvisioning(Cluster cluster) {
        return cluster.supportsGlusterService();
    }

}
