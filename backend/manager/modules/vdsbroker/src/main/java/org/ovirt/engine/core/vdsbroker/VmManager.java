package org.ovirt.engine.core.vdsbroker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.common.action.ExternalDataStatus;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.vdsbroker.monitoring.VdsmVm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmManager {

    private final Guid vmId;

    ///// Static fields ///////
    private String name;
    private OriginType origin;
    private boolean autoStart;
    private int memSizeMb;
    private int minAllocatedMem;
    private int numOfCpus;
    private Version clusterCompatibilityVersion;
    private ArchitectureType clusterArchitecture;
    private BiosType clusterBiosType;

    /** Locks the VM for changes of its dynamic properties */
    private final Lock vmLock;
    /** Locks the VM devices for changes of their dynamic properties (addresses, plugged/unplugged) */
    private final Lock vmDevicesLock;

    private Long vmDataChangedTime;
    /** how long to wait for a response for power-off operation, in nanoseconds */
    private long powerOffTimeout;

    private int convertOperationProgress;
    private String convertOperationDescription;
    private Guid convertProxyHostId;

    private Double lastStatusEventTimestamp;
    private Guid lastStatusEventReporterId;
    private VmStatistics statistics;

    private boolean coldReboot;

    private ExternalDataStatus externalDataStatus;

    /**
     * vmOverhead contains the last known VM memory impact incl. QEMU overhead prediction.
     *
     * The value is computed (and persisted for future use)
     */
    private int vmMemoryWithOverheadInMB;

    private VMStatus lastStatusBeforeMigration;

    private Set<Guid> devicesBeingHotUnplugged;

    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmStatisticsDao vmStatisticsDao;
    @Inject
    private VmNetworkStatisticsDao vmNetworkStatisticsDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private VmOverheadCalculator vmOverheadCalculator;

    VmManager(Guid vmId) {
        this.vmId = vmId;
        vmLock = new ReentrantLock();
        vmDevicesLock = new VmDevicesLock();
        convertOperationProgress = -1;
        statistics = new VmStatistics(vmId);
        vmMemoryWithOverheadInMB = 0;
        externalDataStatus = new ExternalDataStatus();
        devicesBeingHotUnplugged = new HashSet<>();
    }

    @PostConstruct
    public void init() {
        setPowerOffTimeout(System.nanoTime());
        VmStatic vmStatic = vmStaticDao.get(vmId);
        // vmStatic is null for externally managed VMs
        if (vmStatic != null) {
            updateStaticFields(vmStatic);
        }
    }

    private void updateStaticFields(VmStatic vmStatic) {
        name = vmStatic.getName();
        origin = vmStatic.getOrigin();
        autoStart = vmStatic.isAutoStartup();
        memSizeMb = vmStatic.getMemSizeMb();
        minAllocatedMem = vmStatic.getMinAllocatedMem();
        numOfCpus = vmStatic.getNumOfCpus();
        final Cluster cluster = clusterDao.get(vmStatic.getClusterId());
        clusterCompatibilityVersion = cluster.getCompatibilityVersion();
        clusterArchitecture = cluster.getArchitecture();
        clusterBiosType = cluster.getBiosType();

        vmMemoryWithOverheadInMB = estimateOverhead(vmStatic);
    }

    private int estimateOverhead(VmStatic vmStatic) {
        // Prepare VM object using the available bits and pieces
        VM compose = new VM(vmStatic, new VmDynamic(), new VmStatistics(),
                clusterArchitecture, clusterCompatibilityVersion, clusterBiosType);

        // Load device list, TODO ignores unmanaged devices for now
        Map<Guid, VmDevice> devices = vmDeviceDao.getVmDeviceByVmId(vmId).stream()
                .filter(VmDevice::isManaged)
                .collect(Collectors.toMap(d -> d.getId().getDeviceId(), Function.identity()));
        vmStatic.setManagedDeviceMap(devices);

        return vmOverheadCalculator.getTotalRequiredMemMb(compose);
    }

    public void lockVm() {
        vmLock.lock();
    }

    public void unlockVm() {
        vmLock.unlock();
    }

    public boolean tryLockVm() {
        return vmLock.tryLock();
    }

    public Lock getVmDevicesLock() {
        return vmDevicesLock;
    }

    public void update(VmDynamic dynamic) {
        vmDynamicDao.update(dynamic);
    }

    public void update(VmStatistics statistics) {
        vmStatisticsDao.update(statistics);
        setStatistics(statistics);
    }

    public void update(VmNetworkStatistics networkStatistics) {
        vmNetworkStatisticsDao.update(networkStatistics);
    }

    public void update(VmStatic vmStatic) {
        vmStaticDao.update(vmStatic);
        updateStaticFields(vmStatic);
    }

    /**
     * getVmOverheadInMB returns the currently cached value of predicted QEMU overhead for this VM
     *
     * @return the amount of RAM needed for VM including QEMU overhead
     *         (devices, buffers, caches, ..)
     */
    public int getVmMemoryWithOverheadInMB() {
        return vmMemoryWithOverheadInMB;
    }

    public int getConvertOperationProgress() {
        return convertOperationProgress;
    }

    public String getConvertOperationDescription() {
        return convertOperationDescription;
    }

    public void updateConvertOperation(String description, int progress) {
        this.convertOperationDescription = description;
        this.convertOperationProgress = progress;
    }

    public void setConvertProxyHostId(Guid convertProxyHostId) {
        this.convertProxyHostId = convertProxyHostId;
    }

    public Guid getConvertProxyHostId() {
        return convertProxyHostId;
    }

    public Long getVmDataChangedTime () {
        return vmDataChangedTime;
    }

    /**
     * set the changed time of the vm data to the current System.nanoTime()
     * nanoTime should be used as it is more accurate and monotonic,
     *
     * in general this should be called while holding the manager lock
     */
    public final void updateVmDataChangedTime() {
        vmDataChangedTime = System.nanoTime();
    }

    /**
     * Check whether the given data is the latest we got from the given host
     * @param vdsmVm - the data received
     * @param vdsId - the host that sent the data
     * @return false if newer data was already processed, true otherwise
     */
    public boolean isLatestData(VdsmVm vdsmVm, Guid vdsId) {
        if (vdsmVm == null) {
            // VM disappeared from VDSM, we need to have monitoring cycle
            return true;
        }
        Double statusEventTimestamp = vdsmVm.getTimestamp();
        if (!vdsId.equals(lastStatusEventReporterId) || lastStatusEventTimestamp <= statusEventTimestamp) {
            lastStatusEventTimestamp = statusEventTimestamp;
            lastStatusEventReporterId = vdsId;
            return true;
        }
        return false;
    }

    void clearLastStatusEventStampIfFromVds(Guid vdsId) {
        if (vdsId.equals(lastStatusEventReporterId)) {
            lastStatusEventReporterId = null;
        }
    }

    public boolean isColdReboot() {
        return coldReboot;
    }

    public void setColdReboot(boolean coldReboot) {
        this.coldReboot = coldReboot;
    }

    public VmStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(VmStatistics statistics) {
        this.statistics = statistics;
    }

    public String getName() {
        return name;
    }

    public OriginType getOrigin() {
        return origin;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public int getMemSizeMb() {
        return memSizeMb;
    }

    public int getMinAllocatedMem() {
        return minAllocatedMem;
    }

    public int getNumOfCpus() {
        return numOfCpus;
    }

    public void setClusterCompatibilityVersion(Version version) {
        this.clusterCompatibilityVersion = version;
    }

    public Version getClusterCompatibilityVersion() {
        return clusterCompatibilityVersion;
    }

    public ArchitectureType getClusterArchitecture() {
        return clusterArchitecture;
    }

    public void setClusterArchitecture(ArchitectureType clusterArchitecture) {
        this.clusterArchitecture = clusterArchitecture;
    }

    public long getPowerOffTimeout() {
        return powerOffTimeout;
    }

    public long setPowerOffTimeout(long powerOffTimeout) {
        long result = this.powerOffTimeout;
        this.powerOffTimeout = powerOffTimeout;
        return result;
    }

    public String getStopReason(Guid vmId) {
        return vmDynamicDao.get(vmId).getStopReason();
    }

    public ExternalDataStatus getExternalDataStatus() {
        return externalDataStatus;
    }

    public void resetExternalDataStatus() {
        externalDataStatus = new ExternalDataStatus();
    }

    public VMStatus getLastStatusBeforeMigration() {
        return lastStatusBeforeMigration;
    }

    public void setLastStatusBeforeMigration(VMStatus lastStatusBeforeMigration) {
        this.lastStatusBeforeMigration = lastStatusBeforeMigration;
    }

    public boolean isDeviceBeingHotUnlugged(Guid deviceId) {
        synchronized (devicesBeingHotUnplugged) {
            return devicesBeingHotUnplugged.contains(deviceId);
        }
    }

    public void setDeviceBeingHotUnlugged(Guid deviceId, boolean beingHotUnplugged) {
        synchronized (devicesBeingHotUnplugged) {
            if (beingHotUnplugged) {
                devicesBeingHotUnplugged.add(deviceId);
            } else {
                devicesBeingHotUnplugged.remove(deviceId);
            }
        }
    }

    public void rebootCleanup() {
        synchronized (devicesBeingHotUnplugged) {
            devicesBeingHotUnplugged.clear();
        }
    }

    private class VmDevicesLock extends ReentrantLock {
        protected Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void lock() {
            log.debug("locking vm devices monitoring for VM {}", vmId);
            super.lock();
        }

        @Override
        public boolean tryLock() {
            if (!super.tryLock()) {
                log.debug("failed to lock vm devices monitoring for VM {}", vmId);
                return false;
            }
            return true;
        }

        @Override
        public void unlock() {
            log.debug("unlocking vm devices monitoring for VM {}", vmId);
            super.unlock();
        }
    }
}
