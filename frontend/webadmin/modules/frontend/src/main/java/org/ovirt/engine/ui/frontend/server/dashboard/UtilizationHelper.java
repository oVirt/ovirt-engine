package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.List;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.UtilizedEntity.Trend;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.HostDwhDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.StorageDomainDwhDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.VmDwhDao;
import org.ovirt.engine.ui.frontend.server.dashboard.models.TrendResources;

public class UtilizationHelper {

    public static void populateCpuUtilization(Utilization utilization, DataSource dataSource)
            throws DashboardDataException {
        populateCpuUtilizationHosts(utilization, dataSource);
        populateCpuUtilizationVms(utilization, dataSource);
    }

    public static void populateCpuUtilizationHosts(Utilization utilization, DataSource dwhDataSource)
            throws DashboardDataException {
        HostDwhDao dao = new HostDwhDao(dwhDataSource);
        List<TrendResources> resourceUsage = dao.getCpuUtilizationHosts();
        for(TrendResources usage : resourceUsage) {
            UtilizedEntity entity = new UtilizedEntity();
            entity.setName(usage.getName());
            entity.setUsed(usage.getUsed() * usage.getTotal() / 100);
            entity.setTotal(usage.getTotal());
            entity.setTrend(calculateTrend(usage.getUsed(), usage.getPreviousUsed()));
            utilization.addResource(entity);
        }
    }

    public static void populateCpuUtilizationVms(Utilization utilization, DataSource dwhDataSource)
            throws DashboardDataException {
        VmDwhDao dao = new VmDwhDao(dwhDataSource);
        List<TrendResources> usageList = dao.getCpuUtilizationVms();
        for (TrendResources usage: usageList) {
            UtilizedEntity entity = new UtilizedEntity();
            entity.setName(usage.getName());
            entity.setUsed(usage.getUsed());
            entity.setTotal(usage.getTotal());
            entity.setTrend(calculateTrend(usage.getUsed(), usage.getPreviousUsed()));
            utilization.addVm(entity);
        }
    }

    public static void populateMemoryUtilization(Utilization utilization, DataSource dataSource)
            throws DashboardDataException {
        populateMemoryUtilizationHosts(utilization, dataSource);
        populateMemoryUtilizationVms(utilization, dataSource);
    }

    public static void populateMemoryUtilizationHosts(Utilization utilization, DataSource dwhDataSource)
            throws DashboardDataException {
        HostDwhDao dao = new HostDwhDao(dwhDataSource);
        List<TrendResources> usageList = dao.getMemoryUtilizationHosts();
        for (TrendResources usage: usageList) {
            UtilizedEntity entity = new UtilizedEntity();
            entity.setName(usage.getName());
            //Transform to GB
            entity.setUsed(usage.getUsed() / 1024);
            //Transform to GB
            entity.setTotal(usage.getTotal() / 1024);
            entity.setTrend(calculateTrend(usage.getUsed(), usage.getPreviousUsed()));
            utilization.addResource(entity);
        }
    }

    public static void populateMemoryUtilizationVms(Utilization utilization, DataSource dwhDataSource)
            throws DashboardDataException {
        VmDwhDao dao = new VmDwhDao(dwhDataSource);
        List<TrendResources> usageList = dao.getMemoryUtilizationVms();
        for (TrendResources usage: usageList) {
            UtilizedEntity entity = new UtilizedEntity();
            entity.setName(usage.getName());
            //Transform to GB
            entity.setUsed(usage.getUsed() / 1024);
            //Transform to GB
            entity.setTotal(usage.getTotal() / 1024);
            entity.setTrend(calculateTrend(usage.getUsed(), usage.getPreviousUsed()));
            utilization.addVm(entity);
        }
    }

    public static void populateStorageUtilization(Utilization utilization, DataSource dataSource)
            throws DashboardDataException {
        populateStorageUtilizationDomains(utilization, dataSource);
        populateStorageUtilizationVms(utilization, dataSource);
    }

    public static void populateStorageUtilizationDomains(Utilization utilization, DataSource dwhDataSource)
            throws DashboardDataException {
        StorageDomainDwhDao dao = new StorageDomainDwhDao(dwhDataSource);
        List<TrendResources> usageList = dao.getStorageDomainUtilization();
        for(TrendResources usage: usageList) {
            UtilizedEntity entity = new UtilizedEntity();
            entity.setName(usage.getName());
            //Dividing by 1024 to report TB instead of GB
            entity.setUsed(usage.getUsed() / 1024);
            entity.setTotal(usage.getTotal() / 1024);
            entity.setTrend(calculateTrend(usage.getUsed(), usage.getPreviousUsed()));
            utilization.addResource(entity);
        }
    }

    public static void populateStorageUtilizationVms(Utilization utilization, DataSource dwhDataSource)
            throws DashboardDataException {
        StorageDomainDwhDao dao = new StorageDomainDwhDao(dwhDataSource);
        List<TrendResources> usageList = dao.getStorageUtilizationVms();
        for (TrendResources usage: usageList) {
            UtilizedEntity entity = new UtilizedEntity();
            entity.setName(usage.getName());
            //Transform GB to TB
            entity.setUsed(usage.getUsed() / 1024);
            entity.setTotal(usage.getTotal() / 1024);
            entity.setTrend(calculateTrend(usage.getUsed() / usage.getTotal() * 100,
                    usage.getPreviousUsed()));
            utilization.addVm(entity);
        }
    }

    private static Trend calculateTrend(double current, double previous) {
        Trend result = Trend.UP;
        if (Math.abs(current - previous) < 0.001) { //If difference is smaller than delta they are the same.
            result = Trend.SAME;
        } else if (current < previous) {
            result = Trend.DOWN;
        }
        return result;
    }

}
