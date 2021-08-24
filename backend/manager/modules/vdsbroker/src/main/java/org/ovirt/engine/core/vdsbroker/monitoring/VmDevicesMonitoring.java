package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.ArrayList;
import java.util.Collections;
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
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.qualifiers.VmDeleted;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmDevicesMonitoring {

    private enum DevicesChange {
        NOT_CHANGED,
        CHANGED,
        HASH_ONLY
    }

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

        private List<Guid> vmsToProcess;
        private List<VmDevice> devicesToProcess;

        private List<VmDevice> devicesToAdd;
        private List<VmDevice> devicesToUpdate;
        private List<VmDeviceId> deviceIdsToRemove;
        private List<Guid> vmsToSaveHash;

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

        private List<Guid> getVmsToProcess() {
            return getOptionalList(vmsToProcess);
        }

        private void addVmToProcess(Guid vmId) {
            vmsToProcess = addToOptionalList(vmsToProcess, vmId);
        }

        private List<VmDevice> getDevicesToProcess() {
            return getOptionalList(devicesToProcess);
        }

        private void addDeviceToProcess(VmDevice device) {
            devicesToProcess = addToOptionalList(devicesToProcess, device);
        }

        private List<VmDevice> getDevicesToAdd() {
            return getOptionalList(devicesToAdd);
        }

        private void addDeviceToAdd(VmDevice device) {
            devicesToAdd = addToOptionalList(devicesToAdd, device);
        }

        private List<VmDevice> getDevicesToUpdate() {
            return getOptionalList(devicesToUpdate);
        }

        private void addDeviceToUpdate(VmDevice device) {
            devicesToUpdate = addToOptionalList(devicesToUpdate, device);
        }

        private List<VmDeviceId> getDeviceIdsToRemove() {
            return getOptionalList(deviceIdsToRemove);
        }

        private void addDeviceIdToRemove(VmDeviceId deviceId) {
            deviceIdsToRemove = addToOptionalList(deviceIdsToRemove, deviceId);
        }

        private List<Guid> getVmsToSaveHash() {
            return getOptionalList(vmsToSaveHash);
        }

        private void addVmToSaveHash(Guid vmId) {
            vmsToSaveHash = addToOptionalList(vmsToSaveHash, vmId);
        }

        /**
         * Add the VM to the list of VMs to be checked for device updates, if device information hash passed in
         * <code>vdsmHash</code> parameter is more recent (in terms of <code>fetchTime</code>) and differs from
         * the hash remembered by {@link VmDevicesMonitoring}. The new hash is remembered after that.
         */
        public void updateVm(Guid vmId, String vdsmHash) {
            DevicesChange devicesChange = isVmDevicesChanged(vmId, vdsmHash, fetchTime);
            switch(devicesChange) {
            case CHANGED:
                if (!tryLockVmDevices(vmId)) {
                    break;
                }
                lockTouchedVm(vmId);
                addVmToProcess(vmId);
                // fallthrough
            case HASH_ONLY:
                addVmToSaveHash(vmId);
            default:
            }
        }

        /**
         * Process FullList VDSM command result and mark the remembered device information hash to be updated
         * as soon as possible. If any hash for this VM is already remembered, ignore this FullList.
         *
         * @param vmInfo FullList VDSM command result
         */
        public void updateVmFromFullList(Map<String, Object> vmInfo) {
            Guid vmId = getVmId(vmInfo);
            if (isVmDevicesChanged(vmId, UPDATE_HASH, fetchTime) == DevicesChange.CHANGED) {
                addVmToSaveHash(vmId);
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
                addDeviceToProcess(device);
            }
        }

        public void removeDevice(VmDeviceId deviceId) {
            if (isVmDeviceChanged(deviceId, fetchTime)) {
                lockTouchedVm(deviceId.getVmId());
                addDeviceIdToRemove(deviceId);
            }
        }

        /**
         * Process the changes and store the result in the DB.
         */
        public void flush() {
            List<Guid> vmIdsToProcess = getVmsToProcess();
            try {
                Map<String, Object>[] vmInfos = getVmInfo(vdsId, vmIdsToProcess);
                if (vmInfos != null) {
                    Stream.of(vmInfos).forEach(this::processFullList);
                }
                getDevicesToProcess().forEach(device -> processDevice(this, device));
                saveDevicesToDb(this);
            } catch (RuntimeException ex) {
                log.error("Failed during vm devices monitoring on host {} error is: {}", vdsId, ex);
                log.error("Exception:", ex);
            } finally {
                unlockTouchedVms();
                vmIdsToProcess.forEach(VmDevicesMonitoring.this::unlockVmDevices);
            }
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

    public static final String EMPTY_HASH = "";
    public static final String UPDATE_HASH = "UPDATE_HASH";

    @Inject
    private FullListAdapter fullListAdapter;

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VmStaticDao vmStaticDao;

    @Inject
    private VmDeviceDao vmDeviceDao;

    @Inject
    private ResourceManager resourceManager;

    private ConcurrentMap<Guid, DevicesStatus> vmDevicesStatuses = new ConcurrentHashMap<>();
    private ConcurrentMap<Guid, ReentrantLock> vmDevicesLocks = new ConcurrentHashMap<>();
    private final Object devicesStatusesLock = new Object();

    @PostConstruct
    private void init() {
        initDevicesStatuses(System.nanoTime());
    }

    void initDevicesStatuses(long fetchTime) {
        getVmDynamicDao().getAllDevicesHashes().forEach(pair -> vmDevicesStatuses.put(pair.getFirst(),
                new DevicesStatus(pair.getSecond(), fetchTime)));
    }

    public void refreshVmDevices(Guid vmId) {
        vmDevicesStatuses.remove(vmId);
    }

    VmDeviceDao getVmDeviceDao() {
        return vmDeviceDao;
    }

    VmDynamicDao getVmDynamicDao() {
        return vmDynamicDao;
    }

    VmStaticDao getVmStaticDao() {
        return vmStaticDao;
    }

    private static <T> List<T> addToOptionalList(List<T> list, T object) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(object);
        return list;
    }

    private static <T> List<T> getOptionalList(List<T> list) {
        return list != null ? list : Collections.emptyList();
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

    private DevicesChange isVmDevicesChanged(Guid vmId, String vdsmHash, long fetchTime) {
        if (vdsmHash == null) {
            return DevicesChange.NOT_CHANGED;
        }

        // This operation is atomic
        synchronized (devicesStatusesLock) {
            DevicesStatus previousStatus = vmDevicesStatuses.get(vmId);
            boolean previousHashUpdate = previousStatus != null && UPDATE_HASH.equals(previousStatus.getHash());
            if (previousStatus == null || previousHashUpdate || fetchTimeBefore(previousStatus.getFetchTime(), fetchTime)) {
                vmDevicesStatuses.put(vmId, new DevicesStatus(vdsmHash, fetchTime));
                if (previousStatus == null || !Objects.equals(previousStatus.getHash(), vdsmHash)) {
                    return previousHashUpdate ? DevicesChange.HASH_ONLY : DevicesChange.CHANGED;
                } else {
                    return DevicesChange.NOT_CHANGED;
                }
            } else {
                return DevicesChange.NOT_CHANGED;
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

    private Map<String, Object>[] getVmInfo(Guid vdsId, List<Guid> vmIds) {
        if (vdsId == null || vmIds.isEmpty()) {
            return null;
        }

        VDSReturnValue vdsReturnValue = fullListAdapter.getVmFullList(vdsId, vmIds, true);
        return vdsReturnValue.getSucceeded() ?
            (Map<String, Object>[]) vdsReturnValue.getReturnValue()
            : new Map[0];
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
            if (dbDevice == null) {
                dbDevice = getByDeviceType((String) vdsmDevice.get(VdsProperties.Device), dbDeviceMap);
                deviceId = dbDevice != null ? dbDevice.getDeviceId() : deviceId;
            }
            String logicalName = getDeviceLogicalName(vmInfo, vdsmDevice);

            if (deviceId == null || dbDevice == null) {
                VmDevice newDevice = buildNewVmDevice(vmId, vdsmDevice, logicalName);
                if (newDevice != null) {
                    change.addDeviceToAdd(newDevice);
                    processedDeviceIds.add(newDevice.getDeviceId());
                }
            } else {
                dbDevice.setPlugged(Boolean.TRUE);
                dbDevice.setAddress(vdsmDevice.get(VdsProperties.Address).toString());
                dbDevice.setAlias(StringUtils.defaultString((String) vdsmDevice.get(VdsProperties.Alias)));
                dbDevice.setLogicalName(logicalName);
                dbDevice.setHostDevice(StringUtils.defaultString((String) vdsmDevice.get(VdsProperties.HostDev)));
                change.addDeviceToUpdate(dbDevice);
                processedDeviceIds.add(deviceId);
            }
        }

        handleRemovedDevices(change, vmId, processedDeviceIds, dbDevices);
    }

    /**
     * Some of the devices need special treatment:
     * virtio-serial: this device was unmanaged before 3.6 and since 3.6 it is managed.
     * if the VM is running while the engine is upgraded we might still get it as unmanaged
     * from VDSM and since we generate IDs for unmanaged devices, we won't be able to find
     * it by its ID. therefore, we check by its type, assuming that there is only one
     * virtio-serial per VM.
     *
     */
    private VmDevice getByDeviceType(String deviceTypeName, Map<?, VmDevice> dbDevices) {
        if (VmDeviceType.VIRTIOSERIAL.getName().equals(deviceTypeName)) {
            return VmDeviceCommonUtils.findVmDeviceByType(dbDevices, deviceTypeName);
        }

        return null;
    }

    private void processDevice(Change change, VmDevice device) {
        List<VmDevice> dbDevices = getVmDeviceDao().getVmDevicesByDeviceId(device.getDeviceId(), device.getVmId());
        if (dbDevices.isEmpty()) {
            change.addDeviceToAdd(device);
        } else {
            change.addDeviceToUpdate(device);
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

    private String getDeviceLogicalName(Map<String, Object> vmInfo, Map<String, Object> device) {
        Guid deviceId = getDeviceId(device);
        Object deviceType = device.get(VdsProperties.Device);
        if (deviceId != null
                && (VmDeviceType.DISK.getName().equals(deviceType) || VmDeviceType.LUN.getName().equals(deviceType))) {
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

            if (device.isManaged()) {
                // We skip balloon device as it's not hot-pluggable/unpluggable and we always have it.
                // The balloon can be added to a running VM in the DB on upgrade.
                if (device.isPlugged() && device.getType() != VmDeviceGeneralType.BALLOON) {
                    device.setPlugged(Boolean.FALSE);
                    device.setAddress("");
                    change.addDeviceToUpdate(device);
                    log.debug("VM '{}' managed pluggable device was unplugged : '{}'", vmId, device);
                } else if (!devicePluggable(device)) {
                    log.error("VM '{}' managed non pluggable device was removed unexpectedly from libvirt: '{}'",
                            vmId, device);
                }
            } else {
                change.addDeviceIdToRemove(device.getId());
                log.debug("VM '{}' unmanaged device was marked for remove : {1}", vmId, device);
                if (VmDeviceGeneralType.MEMORY.equals(device.getType())) {
                    resourceManager.getVmManager(vmId).setDeviceBeingHotUnlugged(device.getDeviceId(), false);
                }
            }
        }
    }

    private static boolean devicePluggable(VmDevice device) {
        return VmDeviceCommonUtils.isDisk(device) || VmDeviceCommonUtils.isBridge(device)
                || VmDeviceCommonUtils.isHostDevInterface(device);
    }

    /**
     * Libvirt gives no address to some special devices, and we know it.
     */
    private static boolean deviceWithoutAddress(VmDevice device) {
        return VmDeviceCommonUtils.isGraphics(device)
                || VmDeviceGeneralType.CONSOLE.equals(device.getType())
                || VmDeviceGeneralType.TPM.equals(device.getType());
    }

    /**
     * Builds a new device structure for the device recognized by libvirt.
     */
    private VmDevice buildNewVmDevice(Guid vmId, Map device, String logicalName) {
        String typeName = (String) device.get(VdsProperties.Type);
        String deviceName = (String) device.get(VdsProperties.Device);

        // do not allow null or empty device or type values
        if (StringUtils.isEmpty(typeName) || StringUtils.isEmpty(deviceName)) {
            log.error("Empty or NULL values were passed for a VM '{}' device, Device is skipped", vmId);
            return null;
        }
        String address = device.get(VdsProperties.Address).toString();
        String alias = StringUtils.defaultString((String) device.get(VdsProperties.Alias));
        Map<String, Object> specParams = (Map<String, Object>) device.get(VdsProperties.SpecParams);
        specParams = specParams != null ? specParams : new HashMap<>();
        Guid newDeviceId = Guid.newGuid();
        VmDeviceId id = new VmDeviceId(newDeviceId, vmId);
        Object deviceReadonlyValue = device.get(VdsProperties.ReadOnly);
        boolean isReadOnly = deviceReadonlyValue != null && Boolean.getBoolean(deviceReadonlyValue.toString());
        VmDevice newDevice = new VmDevice(
                id,
                VmDeviceGeneralType.forValue(typeName),
                deviceName,
                address,
                specParams,
                false,
                true,
                isReadOnly,
                alias,
                null,
                null,
                logicalName);
        if (VmDeviceCommonUtils.isMemory(newDevice)) {
            fixMemorySpecParamsTypes(newDevice);
        }
        log.debug("New device was marked for adding to VM '{}' Device : '{}'", vmId, newDevice);
        return newDevice;
    }

    /**
     * Fix of wrong type of spec params.
     *
     * @see BZ#1452631
     */
    private void fixMemorySpecParamsTypes(VmDevice newDevice) {
        Stream.of(VmDeviceCommonUtils.SPEC_PARAM_NODE, VmDeviceCommonUtils.SPEC_PARAM_SIZE)
                .forEach(specParam -> {
                    final Object value = newDevice.getSpecParams().get(specParam);
                    if (value instanceof Integer) {
                        newDevice.getSpecParams().put(specParam, String.valueOf(value));
                    }
                });
    }

    private void saveDevicesToDb(Change change) {
        if (!change.getDevicesToUpdate().isEmpty()) {
            getVmDeviceDao().updateAllInBatch(change.getDevicesToUpdate());
        }

        if (!change.getDeviceIdsToRemove().isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                getVmDeviceDao().removeAll(change.getDeviceIdsToRemove());
                return null;
            });
        }

        if (!change.getDevicesToAdd().isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                getVmDeviceDao().saveAll(change.getDevicesToAdd());
                return null;
            });
        }

        if (!change.getVmsToSaveHash().isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                        getVmDynamicDao().updateDevicesHashes(change.getVmsToSaveHash().stream()
                                .map(vmId -> new Pair<>(vmId, vmDevicesStatuses.get(vmId).getHash()))
                                .collect(Collectors.toList()));
                return null;
            });
            getVmStaticDao().incrementDbGenerationForVms(change.getVmsToSaveHash());
        }

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

    private boolean tryLockVmDevices(Guid vmId) {
        return resourceManager.getVmManager(vmId).getVmDevicesLock().tryLock();
    }

    private void unlockVmDevices(Guid vmId) {
        resourceManager.getVmManager(vmId).getVmDevicesLock().unlock();
    }
}
