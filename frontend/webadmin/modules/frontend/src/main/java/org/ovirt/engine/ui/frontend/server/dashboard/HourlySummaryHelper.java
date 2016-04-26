package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.dao.HostDwhDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.StorageDomainDwhDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.VmDwhDao;
import org.ovirt.engine.ui.frontend.server.dashboard.models.ResourceUsage;
import org.ovirt.engine.ui.frontend.server.dashboard.models.ResourcesTotal;
import org.ovirt.engine.ui.frontend.server.dashboard.models.VmStorage;

public class HourlySummaryHelper {

    public static void getCpuMemSummary(GlobalUtilization utilization, DataSource dataSource)
            throws DashboardDataException {
        GlobalUtilizationResourceSummary cpuSummary = new GlobalUtilizationCpuSummary();
        GlobalUtilizationResourceSummary memSummary = new GlobalUtilizationResourceSummary();
        getTotalCpuMemCount(cpuSummary, memSummary, dataSource);
        getHourlyCpuMemUsage(cpuSummary, memSummary, dataSource);
        getVirtualCpuMemCount(cpuSummary, memSummary, dataSource);
        UtilizationHelper.populateCpuUtilization(cpuSummary.getUtilization(), dataSource);
        utilization.setCpu(cpuSummary);
        UtilizationHelper.populateMemoryUtilization(memSummary.getUtilization(), dataSource);
        utilization.setMemory(memSummary);
    }

    private static void getVirtualCpuMemCount(GlobalUtilizationResourceSummary cpuSummary,
            GlobalUtilizationResourceSummary memSummary, DataSource dwhDataSource) throws DashboardDataException {
        VmDwhDao dao = new VmDwhDao(dwhDataSource);
        ResourcesTotal resourcesTotal = dao.getVirtualCpuMemCount();
        cpuSummary.setVirtualTotal(resourcesTotal.getCpuTotal());
        cpuSummary.setVirtualUsed(resourcesTotal.getCpuUsed());
        memSummary.setVirtualTotal(resourcesTotal.getMemTotal());
        memSummary.setVirtualUsed(resourcesTotal.getMemUsed());
    }

    private static void getTotalCpuMemCount(GlobalUtilizationResourceSummary cpuSummary,
            GlobalUtilizationResourceSummary memSummary, DataSource dwhDataSource) throws DashboardDataException {
        HostDwhDao dao = new HostDwhDao(dwhDataSource);
        ResourcesTotal total = dao.getTotalCpuMemCount();
        cpuSummary.setPhysicalTotal(total.getCpuTotal());
        //Transform MB to GB.
        memSummary.setPhysicalTotal(total.getMemTotal() / 1024);
    }

    private static void getHourlyCpuMemUsage(GlobalUtilizationResourceSummary cpuSummary,
            GlobalUtilizationResourceSummary memSummary, DataSource dataSource) throws DashboardDataException {
        List<HistoryNode> cpuHistory = new ArrayList<>();
        List<HistoryNode> memHistory = new ArrayList<>();
        HostDwhDao dao = new HostDwhDao(dataSource);
        List<ResourceUsage> history = dao.getHourlyCpuMemUsage();
        for (ResourceUsage item: history) {
            cpuHistory.add(new HistoryNode(item.getEpoch(), item.getCpuValue()));
            memHistory.add(new HistoryNode(item.getEpoch(), item.getMemValue() * memSummary.getTotal() / 100));
        }
        ResourceUsage last5minUsage = dao.getLast5MinCpuMemUsage();
        cpuSummary.setUsed(last5minUsage.getCpuValue());
        memSummary.setUsed(last5minUsage.getMemValue() * memSummary.getTotal() / 100);
        cpuSummary.setHistory(cpuHistory);
        memSummary.setHistory(memHistory);
    }

    public static GlobalUtilizationResourceSummary getStorageSummary(DataSource dataSource)
            throws DashboardDataException {
        GlobalUtilizationResourceSummary result = new GlobalUtilizationResourceSummary(new StorageUtilization());
        result.setPhysicalTotal(getTotalStorageCount(dataSource));
        result.setHistory(getHourlyStorageHistory(dataSource));
        result.setUsed(getLast5MinutesStorageAverage(dataSource));
        getVirtualStorageCount(result, dataSource);
        UtilizationHelper.populateStorageUtilization(result.getUtilization(), dataSource);
        return result;
    }

    private static List<HistoryNode> getHourlyStorageHistory(DataSource dwhDataSource) throws DashboardDataException {
        List<HistoryNode> history = new ArrayList<>();
        StorageDomainDwhDao dao = new StorageDomainDwhDao(dwhDataSource);
        List<ResourceUsage> usageList = dao.getHourlyStorageHistory();
        for (ResourceUsage usage: usageList) {
            //Transform GB to TB.
            history.add(new HistoryNode(usage.getEpoch(), usage.getStorageValue() / 1024));
        }
        return history;
    }

    private static double getLast5MinutesStorageAverage(DataSource dwhDataSource) throws DashboardDataException {
        StorageDomainDwhDao dao = new StorageDomainDwhDao(dwhDataSource);
        //Transform GB to TB.
        return dao.getLast5MinutesStorageAverage() / 1024;
    }

    private static Double getTotalStorageCount(DataSource dwhDataSource) throws DashboardDataException {
        StorageDomainDwhDao dao = new StorageDomainDwhDao(dwhDataSource);
        //Transform GB to TB.
        return dao.getTotalStorageCount() / 1024;
    }

    private static void getVirtualStorageCount(GlobalUtilizationResourceSummary storageSummary,
            DataSource dwhDataSource) throws DashboardDataException {
        VmDwhDao dao = new VmDwhDao(dwhDataSource);
        VmStorage storageCount = dao.getVirtualStorageCount();
        storageSummary.setVirtualUsed(storageCount.getUsed());
        storageSummary.setVirtualTotal(storageCount.getTotal());
    }

}
