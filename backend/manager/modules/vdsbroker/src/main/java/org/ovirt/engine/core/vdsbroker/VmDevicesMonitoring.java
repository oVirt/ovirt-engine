package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.qualifiers.VmDeleted;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmDevicesMonitoring implements BackendService {

    /**
     * This class describes a change in VM devices that needs to be processed by {@link VmDevicesMonitoring}.
     * <p>
     * Instances of this class must be created by {@link #createChange} method.
     * <p>
     * The change may be represented in two forms (or mix of both):
     * <ul>
     *     <li>
     *         Individual additions/updates/removals of devices. Use {@link #updateDevice} (for both additions and
     *         updates) and {@link #removeDevice} methods to add them one by one.
     *     </li>
     *     <li>
     *         VMs as whole added by {@link #updateVm} method. In this case, <code>FullList</code> query is sent to
     *         the corresponding VDSM returning list of devices for each VM. This list is then compared to the one
     *         in the DB to detect individual changes. (<b>Note</b>: this works only if <code>vdsId</code> was set).
     *     </li>
     * </ul>
     * After adding all the changes, call {@link #flush} to process them and store the result in the DB.
     */
    public class Change {

        private Guid vdsId;

        private List<Guid> vmsToUpdate = new ArrayList<>();
        private List<VmDevice> devicesToUpdate = new ArrayList<>();

        private List<VmDevice> addedDevices = new ArrayList<>();
        private List<VmDevice> updatedDevices = new ArrayList<>();
        private List<VmDeviceId> removedDeviceIds = new ArrayList<>();

        private Deque<Guid> touchedVms = new LinkedList<>();

        private long fetchTime;

        private Change(long fetchTime) {
            this.fetchTime = fetchTime;
        }

        private Change(Guid vdsId, long fetchTime) {
            this.vdsId = vdsId;
            this.fetchTime = fetchTime;
        }

        public Guid getVdsId() {
            return vdsId;
        }

        private void lockTouchedVm(Guid vmId) {
            if (lockOnce(vmId)) {
                touchedVms.push(vmId);
            }
        }

        private void unlockTouchedVms() {
            touchedVms.forEach(VmDevicesMonitoring.this::unlock);
        }

        /**
         * Add the VM to the list of VMs to be checked for device updates, if device information hash passed in
         * <code>vdsmHash</code> parameter is more recent (in terms of <code>fetchTime</code>) and differs from
         * the hash remembered by {@link VmDevicesMonitoring}. The new hash is remembered after that.
         */
        public void updateVm(Guid vmId, String vdsmHash) {
            if (!VmDeviceCommonUtils.isOldClusterVersion(getGroupCompatibilityVersion(vdsId))
                    && isVmDevicesChanged(vmId, vdsmHash, fetchTime)) {
                lockTouchedVm(vmId);
                vmsToUpdate.add(vmId);
            }
        }

        /**
         * Process FullList VDSM command result and mark the remembered device information hash to be updated
         * as soon as possible. If any hash for this VM is already remembered, ignore this FullList.
         *
         * @param vmInfo FullList VDSM command result
         */
        public void updateVmFromFullList(Map<String, Object> vmInfo) {
            if (!VmDeviceCommonUtils.isOldClusterVersion(getGroupCompatibilityVersion(vdsId))
                    && isVmDevicesChanged(getVmId(vmInfo), UPDATE_HASH, fetchTime)) {
                processFullList(vmInfo);
            }
        }

        /**
         * Process FullList VDSM command result and add/remove/update devices in accordance to the information in it.
         *
         * @param vmInfo FullList VDSM command result
         */
        private void processFullList(Map<String, Object> vmInfo) {
            Guid vmId = getVmId(vmInfo);
            if (vmId == null) {
                log.error("Received NULL VM or VM id when processing VM devices, abort.");
                return;
            }

            lockTouchedVm(vmId);
            processVmDevices(this, vmInfo);
        }

        public void updateDevice(VmDevice device) {
            if (isVmDeviceChanged(device.getId(), fetchTime)) {
                lockTouchedVm(device.getVmId());
                devicesToUpdate.add(device);
            }
        }

        public void removeDevice(VmDeviceId deviceId) {
            if (isVmDeviceChanged(deviceId, fetchTime)) {
                lockTouchedVm(deviceId.getVmId());
                removedDeviceIds.add(deviceId);
            }
        }

        /**
         * Process the changes and store the result in the DB.
         */
        public void flush() {
            Map<String, Object>[] vmInfos = getVmInfo(vdsId, vmsToUpdate);
            if (vmInfos != null) {
                for (Map<String, Object> vmInfo : vmInfos) {
                    processFullList(vmInfo);
                }
            }
            for (VmDevice deviceToUpdate : devicesToUpdate) {
                processDeviceUpdate(this, deviceToUpdate);
            }
            saveDevicesToDb(this);
            unlockTouchedVms();
        }

    }

    private static class DevicesStatus {

        private String hash;
        private Long fetchTime;
        private Map<Guid, Long> deviceFetchTimes;

        public DevicesStatus() {
            this(EMPTY_HASH, null);
        }

        public DevicesStatus(String hash, Long fetchTime) {
            this.hash = hash;
            this.fetchTime = fetchTime;
        }

        public String getHash() {
            return hash;
        }

        public Long getFetchTime() {
            return fetchTime;
        }


        public Long getDeviceFetchTime(Guid deviceId) {
            return deviceFetchTimes != null ? deviceFetchTimes.getOrDefault(deviceId, fetchTime) : fetchTime;
        }

        public void setDeviceFetchTime(Guid deviceId, Long fetchTime) {
            if (fetchTime != null) {
                if (deviceFetchTimes == null) {
                    deviceFetchTimes = new HashMap<>();
                }
                deviceFetchTimes.put(deviceId, fetchTime);
            }
        }

    }

    private static final Logger log = LoggerFactory.getLogger(VmDevicesMonitoring.class);

    private static VmDevicesMonitoring instance;

    private static String EMPTY_HASH = "";
    private static String UPDATE_HASH = "UPDATE_HASH";

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VmDeviceDao vmDeviceDao;

    private ConcurrentMap<Guid, DevicesStatus> vmDevicesStatuses = new ConcurrentHashMap<>();
    private ConcurrentMap<Guid, ReentrantLock> vmDevicesLocks = new ConcurrentHashMap<>();
    private Object devicesStatusesLock = new Object();

    public VmDevicesMonitoring() {
    }

    @PostConstruct
    private void init() {
        instance = this;

        long fetchTime = System.nanoTime();
        getVmDynamicDao().getAllDevicesHashes().forEach(pair -> vmDevicesStatuses.put(pair.getFirst(),
                new DevicesStatus(pair.getSecond(), fetchTime)));
    }

    public static VmDevicesMonitoring getInstance() {
        return instance;
    }

    private ResourceManager getResourceManager() {
        return resourceManager;
    }

    public VmDeviceDao getVmDeviceDao() {
        return vmDeviceDao;
    }

    public VmDynamicDao getVmDynamicDao() {
        return vmDynamicDao;
    }

    public Change createChange(long fetchTime) {
        return new Change(fetchTime);
    }

    public Change createChange(Guid vdsId, long fetchTime) {
        return new Change(vdsId, fetchTime);
    }

    /**
     * This method acquires lock on the VM given, doing this only once per thread. If the lock is already held by
     * the current thread, this method just returns false. If not, the method tries to acquire the lock. If the lock
     * is already held by another thread, this method blocks the current thread until the lock becomes available.
     *
     * @return true, if the lock was actually taken for the first time, false otherwise
     */
    private boolean lockOnce(Guid vmId) {
        vmDevicesLocks.computeIfAbsent(vmId, guid -> new ReentrantLock());
        ReentrantLock lock = vmDevicesLocks.get(vmId);
        if (!lock.isHeldByCurrentThread()) {
            lock.lock();
            return true;
        } else {
            return false;
        }
    }

    private void unlock(Guid vmId) {
        Lock lock = vmDevicesLocks.get(vmId);
        if (lock != null) {
            lock.unlock();
        } else {
            log.warn("Attempt to release non-existent lock for VM {}", vmId);
        }
    }

    private void removeLock(Guid vmId) {
        Lock lock = vmDevicesLocks.get(vmId);
        if (lock != null) {
            lock.lock();
            try {
                vmDevicesLocks.remove(vmId);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Compares two fetch times chronologically. A null fetch time is assumed to be before any non-null fetch time.
     *
     * @return true, if <code>fetchTimeA</code> is before <code>fetchTimeB</code>, false otherwise
     */
    private static boolean fetchTimeBefore(Long fetchTimeA, Long fetchTimeB) {
        if (fetchTimeA == null) {
            return true;
        }
        if (fetchTimeB == null) {
            return false;
        }
        return fetchTimeA - fetchTimeB < 0;
    }

    private boolean isVmDevicesChanged(Guid vmId, String vdsmHash, long fetchTime) {
        if (vdsmHash == null) {
            return false;
        }

        // This operation is atomic
        synchronized (devicesStatusesLock) {
            DevicesStatus previousStatus = vmDevicesStatuses.get(vmId);
            boolean previousHashUpdate = previousStatus != null && UPDATE_HASH.equals(previousStatus.getHash());
            if (previousStatus == null || previousHashUpdate || fetchTimeBefore(previousStatus.getFetchTime(), fetchTime)) {
                vmDevicesStatuses.put(vmId, new DevicesStatus(vdsmHash, fetchTime));
                return previousStatus == null || !previousHashUpdate && !Objects.equals(previousStatus.getHash(), vdsmHash);
            } else {
                return false;
            }
        }
    }

    private boolean isVmDeviceChanged(VmDeviceId deviceId, long fetchTime) {
        // This operation is atomic
        synchronized (devicesStatusesLock) {
            DevicesStatus devicesStatus = vmDevicesStatuses.computeIfAbsent(deviceId.getVmId(),
                    vmId -> new DevicesStatus());
            Long prevFetchTime = devicesStatus.getDeviceFetchTime(deviceId.getDeviceId());
            if (fetchTimeBefore(prevFetchTime, fetchTime)) {
                devicesStatus.setDeviceFetchTime(deviceId.getDeviceId(), fetchTime);
                return true;
            } else {
                return false;
            }
        }
    }

    private void onVmDelete(@Observes @VmDeleted Guid vmId) {
        vmDevicesStatuses.remove(vmId);
        removeLock(vmId);
    }

    private Map<String, Object>[] getVmInfo(Guid vdsId, List<Guid> vms) {
        if (vdsId == null || vms.isEmpty()) {
            return null;
        }

        Map<String, Object>[] result = new Map[0];

        VDS vds = new VDS(); // TODO refactor commands to use vdsId only - the whole vds object here is useless
        vds.setId(vdsId);
        List<String> vmIds = vms.stream().map(guid -> guid.toString()).collect(Collectors.toList());
        VDSReturnValue vdsReturnValue =
                getResourceManager().runVdsCommand(VDSCommandType.FullList,
                        new FullListVDSCommandParameters(vds, vmIds));
        if (vdsReturnValue.getSucceeded()) {
            result = (Map<String, Object>[]) vdsReturnValue.getReturnValue();
        }

        return result;
    }

    /**
     * Actually process the VM device update and store individual device additions/updates/removals
     * in the <code>change</code>.
     */
    private void processVmDevices(Change change, Map<String, Object> vmInfo) {
        Guid vmId = getVmId(vmInfo);
        Set<Guid> processedDeviceIds = new HashSet<>();
        List<VmDevice> dbDevices = getVmDeviceDao().getVmDeviceByVmId(vmId);
        Map<VmDeviceId, VmDevice> dbDeviceMap = Entities.businessEntitiesById(dbDevices);

        for (Object o: (Object[]) vmInfo.get(VdsProperties.Devices)) {
            Map<String, Object> vdsmDevice = (Map<String, Object>) o;

            if (vdsmDevice.get(VdsProperties.Address) == null) {
                logDeviceInformation(vmId, vdsmDevice);
                continue;
            }

            Guid deviceId = getDeviceId(vdsmDevice);
            VmDevice dbDevice = dbDeviceMap.get(new VmDeviceId(deviceId, vmId));
            String logicalName = getDeviceLogicalName(change.getVdsId(), vmInfo, vdsmDevice);

            if (deviceId == null || dbDevice == null) {
                VmDevice newDevice = buildNewVmDevice(vmId, vdsmDevice, logicalName);
                change.addedDevices.add(newDevice);
                deviceId = newDevice.getDeviceId();
            } else {
                dbDevice.setIsPlugged(Boolean.TRUE);
                dbDevice.setAddress(vdsmDevice.get(VdsProperties.Address).toString());
                dbDevice.setAlias(StringUtils.defaultString((String) vdsmDevice.get(VdsProperties.Alias)));
                dbDevice.setLogicalName(logicalName);
                dbDevice.setHostDevice(StringUtils.defaultString((String) vdsmDevice.get(VdsProperties.HostDev)));
                change.updatedDevices.add(dbDevice);
            }

            processedDeviceIds.add(deviceId);
        }

        handleRemovedDevices(change, vmId, processedDeviceIds, dbDevices);
    }

    private void processDeviceUpdate(Change change, VmDevice device) {
        List<VmDevice> dbDevices = getVmDeviceDao().getVmDevicesByDeviceId(device.getDeviceId(), device.getVmId());
        if (dbDevices.isEmpty()) {
            change.addedDevices.add(device);
        } else {
            change.updatedDevices.add(device);
        }
    }

    private static Guid getVmId(Map<String, Object> vmInfo) {
        return vmInfo != null ? new Guid((String) vmInfo.get(VdsProperties.vm_guid)) : null;
    }

    /**
     * Gets the device ID from the structure returned by VDSM.
     */
    private static Guid getDeviceId(Map<String, Object> deviceInfo) {
        String deviceId = (String) deviceInfo.get(VdsProperties.DeviceId);
        return deviceId == null ? null : new Guid(deviceId);
    }

    private String getDeviceLogicalName(Guid vdsId, Map<String, Object> vmInfo, Map<String, Object> device) {
        Guid deviceId = getDeviceId(device);
        if (deviceId != null && FeatureSupported.reportedDisksLogicalNames(getGroupCompatibilityVersion(vdsId))
                && VmDeviceType.DISK.getName().equals(device.get(VdsProperties.Device))) {
            try {
                return getDeviceLogicalName((Map<String, Object>) vmInfo.get(VdsProperties.GuestDiskMapping), deviceId);
            } catch (Exception e) {
                log.error("error while getting device name when processing, vm '{}', device info '{}' with exception, skipping '{}'",
                        vmInfo.get(VdsProperties.vm_guid), device, e.getMessage());
                log.error("Exception", e);
            }
        }
        return null;
    }

    private static String getDeviceLogicalName(Map<String, Object> diskMapping, Guid deviceId) {
        if (diskMapping == null) {
            return null;
        }

        Map<String, Object> deviceMapping = null;
        String modifiedDeviceId = deviceId.toString().substring(0, 20);
        for (Map.Entry<String, Object> entry : diskMapping.entrySet()) {
            String serial = entry.getKey();
            if (serial != null && serial.contains(modifiedDeviceId)) {
                deviceMapping = (Map<String, Object>) entry.getValue();
                break;
            }
        }

        return deviceMapping == null ? null : (String) deviceMapping.get(VdsProperties.Name);
    }

    /**
     * Handles devices that were removed by libvirt. Unmanaged devices are marked to be removed, managed devices are
     * unplugged - the address is cleared and isPlugged is set to false.
     *
     * @param libvirtDevices list of IDs of devices that were returned by libvirt
     * @param dbDevices list of all devices present in the DB
     */
    private void handleRemovedDevices(Change change, Guid vmId, Set<Guid> libvirtDevices, List<VmDevice> dbDevices) {
        for (VmDevice device : dbDevices) {
            if (libvirtDevices.contains(device.getDeviceId())) {
                continue;
            }

            if (deviceWithoutAddress(device)) {
                continue;
            }

            if (device.getIsManaged()) {
                if (device.getIsPlugged()) {
                    device.setIsPlugged(Boolean.FALSE);
                    device.setAddress("");
                    change.updatedDevices.add(device);
                    log.debug("VM '{}' managed pluggable device was unplugged : '{}'", vmId, device);
                } else if (!devicePluggable(device)) {
                    log.error("VM '{}' managed non pluggable device was removed unexpectedly from libvirt: '{}'",
                            vmId, device);
                }
            } else {
                change.removedDeviceIds.add(device.getId());
                log.debug("VM '{}' unmanaged device was marked for remove : {1}", vmId, device);
            }
        }
    }

    private static boolean devicePluggable(VmDevice device) {
        return VmDeviceCommonUtils.isDisk(device) || VmDeviceCommonUtils.isBridge(device);
    }

    /**
     * Libvirt gives no address to some special devices, and we know it.
     */
    private static boolean deviceWithoutAddress(VmDevice device) {
        return VmDeviceCommonUtils.isGraphics(device);
    }

    /**
     * Builds a new device structure for the device recognized by libvirt.
     */
    private VmDevice buildNewVmDevice(Guid vmId, Map device, String logicalName) {
        Guid newDeviceId = Guid.Empty;
        String typeName = (String) device.get(VdsProperties.Type);
        String deviceName = (String) device.get(VdsProperties.Device);
        VmDevice newDevice = null;

        // do not allow null or empty device or type values
        if (StringUtils.isEmpty(typeName) || StringUtils.isEmpty(deviceName)) {
            log.error("Empty or NULL values were passed for a VM '{}' device, Device is skipped", vmId);
        } else {
            String address = device.get(VdsProperties.Address).toString();
            String alias = StringUtils.defaultString((String) device.get(VdsProperties.Alias));
            Object specParams = device.get(VdsProperties.SpecParams);
            newDeviceId = Guid.newGuid();
            VmDeviceId id = new VmDeviceId(newDeviceId, vmId);
            newDevice = new VmDevice(id, VmDeviceGeneralType.forValue(typeName), deviceName, address,
                    0,
                    specParams == null ? new HashMap<>() : (Map<String, Object>) specParams,
                    false,
                    true,
                    Boolean.getBoolean((String) device.get(VdsProperties.ReadOnly)),
                    alias,
                    null,
                    null,
                    logicalName);
            log.debug("New device was marked for adding to VM '{}' Devices : '{}'", vmId, newDevice);
        }

        return newDevice;
    }

    private void saveDevicesToDb(Change change) {
        getVmDeviceDao().updateAllInBatch(change.updatedDevices);

        if (!change.removedDeviceIds.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                getVmDeviceDao().removeAll(change.removedDeviceIds);
                return null;
            });
        }

        if (!change.addedDevices.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                getVmDeviceDao().saveAll(change.addedDevices);
                return null;
            });
        }

        if (!change.touchedVms.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                        getVmDynamicDao().updateDevicesHashes(change.touchedVms.stream()
                                .map(vmId -> new Pair<>(vmId, vmDevicesStatuses.get(vmId).getHash()))
                                .collect(Collectors.toList()));
                return null;
            });
        }

    }

    private Version getGroupCompatibilityVersion(Guid vdsId) {
        return getResourceManager().getVdsManager(vdsId).getGroupCompatibilityVersion();
    }

    private boolean shouldLogDeviceDetails(String deviceType) {
        return !StringUtils.equalsIgnoreCase(deviceType, VmDeviceType.FLOPPY.getName());
    }

    private void logDeviceInformation(Guid vmId, Map<String, Object> device) {
        String message = "Received a {} Device without an address when processing VM {} devices, skipping device";
        String deviceType = (String) device.get(VdsProperties.Device);

        if (shouldLogDeviceDetails(deviceType)) {
            log.info(message + ": {}", StringUtils.defaultString(deviceType), vmId, device);
        } else {
            log.info(message, StringUtils.defaultString(deviceType), vmId);
        }
    }

}
