package org.ovirt.engine.core.vdsbroker;

import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

public class VmManager {

    private final Guid vmId;
    private final ReentrantLock lock;
    private Long vmDataChangedTime;

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

    VmManager(Guid vmId) {
        this.vmId = vmId;
        lock = new ReentrantLock();
        convertOperationProgress = -1;
        statistics = new VmStatistics();
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

    public void succededToHibernate() {
        TransactionSupport.executeInNewTransaction(() -> {
                    VmDynamic vmDynamic = vmDynamicDao.get(vmId);
                    vmDynamic.setStatus(VMStatus.SavingState);
                    update(vmDynamic);
                    return null;
                }
        );
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
     * @param vmInternalData - the data received
     * @param vdsId - the host that sent the data
     * @return false if newer data was already processed, true otherwise
     */
    public boolean isLatestData(VmInternalData vmInternalData, Guid vdsId) {
        if (vmInternalData == null) {
            // VM disappeared from VDSM, we need to have monitoring cycle
            return true;
        }
        Double statusEventTimestamp = vmInternalData.getTimestamp();
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
}
