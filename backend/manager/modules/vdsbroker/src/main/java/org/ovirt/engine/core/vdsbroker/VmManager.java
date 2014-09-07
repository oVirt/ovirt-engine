package org.ovirt.engine.core.vdsbroker;

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

import java.util.concurrent.locks.ReentrantLock;

public class VmManager {

    private final Guid id;
    private final ReentrantLock lock = new ReentrantLock();

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
}
