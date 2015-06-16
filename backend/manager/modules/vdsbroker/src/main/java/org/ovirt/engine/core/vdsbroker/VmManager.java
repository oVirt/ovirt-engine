package org.ovirt.engine.core.vdsbroker;

import java.util.concurrent.locks.ReentrantLock;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmStatisticsDAO;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

public class VmManager {

    private final Guid id;
    private final ReentrantLock lock = new ReentrantLock();
    private Long vmDataChangedTime;

    private int convertOperationProgress;
    private String convertOperationDescription;

    private Double lastStatusEventTimestamp;
    private Guid lastStatusEventReporterId;

    public VmManager(Guid id) {
        this.id = id;
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
        getVmDynamicDao().update(dynamic);
    }

    public void update(VmStatistics statistics) {
        getVmStatisticsDao().update(statistics);
    }

    public void update(VmNetworkStatistics networkStatistics) {
        getVmNetworkStatisticsDao().update(networkStatistics);
    }

    public void succededToHibernate() {
        TransactionSupport.executeInNewTransaction(
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        VmDynamic vmDynamic = getVmDynamicDao().get(id);
                        vmDynamic.setStatus(VMStatus.SavingState);
                        update(vmDynamic);
                        return null;
                    }
                }
        );
    }

    private VmDynamicDAO getVmDynamicDao() {
        return db().getVmDynamicDao();
    }

    private VmStatisticsDAO getVmStatisticsDao() {
        return db().getVmStatisticsDao();
    }

    private VmNetworkStatisticsDao getVmNetworkStatisticsDao() {
        return db().getVmNetworkStatisticsDao();
    }

    protected DbFacade db() {
        return DbFacade.getInstance();
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
    boolean isLatestData(VmInternalData vmInternalData, Guid vdsId) {
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
}
