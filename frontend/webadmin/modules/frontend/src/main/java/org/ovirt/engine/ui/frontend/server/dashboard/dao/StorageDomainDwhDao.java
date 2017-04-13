package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.models.ResourceUsage;
import org.ovirt.engine.ui.frontend.server.dashboard.models.StorageDomainAverage;
import org.ovirt.engine.ui.frontend.server.dashboard.models.TrendResources;

public class StorageDomainDwhDao extends BaseDao {

    private static final String NAME = "name"; //$NON-NLS-1$
    private static final String AVERAGE = "avg"; //$NON-NLS-1$
    private static final String USED = "used"; //$NON-NLS-1$
    private static final String DATE = "the_date"; //$NON-NLS-1$
    private static final String TOTAL = "total"; //$NON-NLS-1$
    private static final String AVAILABLE="available"; //$NON-NLS-1$
    private static final String PREVIOUS_USED="previous_used"; //$NON-NLS-1$
    private static final String PREVIOUS_TOTAL="previous_total"; //$NON-NLS-1$

    private static final String STORAGE_LAST24_AVERAGE = "storage.last24hours_average"; //$NON-NLS-1$
    private static final String HOURLY_STORAGE_HISTORY = "storage.hourly_history"; //$NON-NLS-1$
    private static final String LAST5_MIN_STORAGE_AVERAGE = "storage.last5_minutes_average"; //$NON-NLS-1$
    private static final String TOTAL_STORAGE_COUNT = "storage.total_count"; //$NON-NLS-1$
    private static final String STORAGE_DOMAIN_UTILIZATION = "storage.utilization"; //$NON-NLS-1$
    private static final String VM_STORAGE_UTILIZATION = "storage.vm_utilization"; //$NON-NLS-1$

    public StorageDomainDwhDao(DataSource dwhDataSource) throws DashboardDataException {
        super(dwhDataSource, "StorageDomainDwhDAO.properties", StorageDomainDwhDao.class); //$NON-NLS-1$
    }

    public List<StorageDomainAverage> getStorageAverage() throws DashboardDataException {
        final List<StorageDomainAverage> result = new ArrayList<>();

        runQuery(STORAGE_LAST24_AVERAGE,
                rs -> result.add(new StorageDomainAverage(rs.getString(NAME), rs.getDouble(AVERAGE))));

        return result;
    }

    public List<ResourceUsage> getHourlyStorageHistory() throws DashboardDataException {
        final List<ResourceUsage> history = new ArrayList<>();

        runQuery(HOURLY_STORAGE_HISTORY, rs -> {
            ResourceUsage usage = new ResourceUsage();
            usage.setEpoch(rs.getTimestamp(DATE).getTime());
            usage.setStorageValue(rs.getDouble(USED));
            history.add(usage);
        });

        return history;
    }

    /**
     * Returns the average storage domain usage over the last 5 minute in GB.
     * @return A double indicating the average usage over 5 minutes in GB.
     * @throws DashboardDataException If a database issue occurs.
     */
    public double getLast5MinutesStorageAverage() throws DashboardDataException {
        final double[] result = {0};

        runQuery(LAST5_MIN_STORAGE_AVERAGE, rs -> result[0] = rs.getDouble(USED));

        return result[0];
    }

    /**
     * Returns the total storage domain storage over all storage domains in GB.
     * @return The total storage domain value over all storage domains in GB.
     * @throws DashboardDataException If a database issue occurs.
     */
    public Double getTotalStorageCount() throws DashboardDataException {
        final Double[] result = {0.0};

        runQuery(TOTAL_STORAGE_COUNT, rs -> result[0] = rs.getDouble(TOTAL));

        return result[0];
    }

    /**
     * Get the CPU utilization for the last 5 minutes per storage domain. Also retrieve the previous 5 minutes
     * before that, so we can calculate a trend. Usage in returned in GB. Top 10 are returned.
     * @return List of {@code TrendResources} objects containing usage of each storage domain.
     * @throws DashboardDataException If there is a database problem.
     */
    public List<TrendResources> getStorageDomainUtilization() throws DashboardDataException {
        final List<TrendResources> result = new ArrayList<>();

        runQuery(STORAGE_DOMAIN_UTILIZATION, rs -> {
            TrendResources usage = new TrendResources();
            usage.setName(rs.getString(NAME));
            usage.setUsed(rs.getDouble(USED));
            usage.setTotal(rs.getDouble(USED) + rs.getDouble(AVAILABLE));
            usage.setPreviousUsed(rs.getDouble(PREVIOUS_USED));
            result.add(usage);
        });

        return result;
    }

    /**
     * Get storage utilization for the last 5 minutes per storage domain. Also retrieve the previous 5 minute
     * before that, so we can calculate a trend. Usage is returned in GB. Top 10 are returned
     * @return List of {@code TrendResources} objects containing usage of each VM.
     * @throws DashboardDataException If there is a database problem.
     */
    public List<TrendResources> getStorageUtilizationVms() throws DashboardDataException {
        final List<TrendResources> result = new ArrayList<>();

        runQuery(VM_STORAGE_UTILIZATION, rs -> {
            TrendResources usage = new TrendResources();
            usage.setName(rs.getString(NAME));
            usage.setUsed(rs.getDouble(USED) / 1024);
            usage.setTotal(rs.getDouble(TOTAL) / 1024);
            usage.setPreviousUsed(rs.getDouble(PREVIOUS_USED) / rs.getDouble(PREVIOUS_TOTAL) * 100);
            result.add(usage);
        });

        return result;
    }

}
