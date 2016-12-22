package org.ovirt.engine.core.vdsbroker;

import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.vdsbroker.monitoring.VdsmVm;

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
    private Guid leaseStorageDomainId;

    private final ReentrantLock lock;
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

    VmManager(Guid vmId) {
        this.vmId = vmId;
        lock = new ReentrantLock();
        convertOperationProgress = -1;
        statistics = new VmStatistics(vmId);
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
        clusterCompatibilityVersion = clusterDao.get(vmStatic.getClusterId()).getCompatibilityVersion();
        leaseStorageDomainId = vmStatic.getLeaseStorageDomainId();
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean trylock() {
        return lock.tryLock();
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

    public Guid getLeaseStorageDomainId() {
        return leaseStorageDomainId;
    }

    public long getPowerOffTimeout() {
        return powerOffTimeout;
    }

    public long setPowerOffTimeout(long powerOffTimeout) {
        long result = this.powerOffTimeout;
        this.powerOffTimeout = powerOffTimeout;
        return result;
    }

}
