package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.models.ResourceUsage;
import org.ovirt.engine.ui.frontend.server.dashboard.models.StorageDomainAverage;
import org.ovirt.engine.ui.frontend.server.dashboard.models.TrendResources;

public class StorageDomainDwhDAO {
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

    private final DataSource dwhDataSource;
    private final Properties storageProperties;

    public StorageDomainDwhDAO(DataSource dwhDataSource) throws DashboardDataException {
        this.dwhDataSource = dwhDataSource;
        storageProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("StorageDomainDwhDAO.properties")) { //$NON-NLS-1$
            storageProperties.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load DAO queries"); //$NON-NLS-1$
        }
    }

    public List<StorageDomainAverage> getStorageAverage() throws SQLException {
        List<StorageDomainAverage> result = new ArrayList<>();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement storageAveragePS = con.prepareStatement(
                     storageProperties.getProperty(STORAGE_LAST24_AVERAGE));
             ResultSet storageAverageRS = storageAveragePS.executeQuery()) {
            while (storageAverageRS.next()) {
                result.add(new StorageDomainAverage(storageAverageRS.getString(NAME),
                        storageAverageRS.getDouble(AVERAGE)));
            }
        }
        return result;
    }

    public List<ResourceUsage> getHourlyStorageHistory() throws SQLException {
        List<ResourceUsage> history = new ArrayList<>();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement storageHistoryPS = con.prepareStatement(
                     storageProperties.getProperty(HOURLY_STORAGE_HISTORY));
             ResultSet storageHistoryRS = storageHistoryPS.executeQuery()) {
            while (storageHistoryRS.next()) {
                ResourceUsage usage = new ResourceUsage();
                usage.setEpoch(storageHistoryRS.getTimestamp(DATE).getTime());
                usage.setStorageValue(storageHistoryRS.getDouble(USED));
                history.add(usage);
            }
        }
        return history;
    }

    /**
     * Returns the average storage domain usage over the last 5 minute in GB.
     * @return A double indicating the average usage over 5 minutes in GB.
     * @throws SQLException If a database issue occurs.
     */
    public double getLast5MinutesStorageAverage() throws SQLException {
        double result = 0;
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement last5minutesPs = con.prepareStatement(
                     storageProperties.getProperty(LAST5_MIN_STORAGE_AVERAGE));
             ResultSet last5minutesRs = last5minutesPs.executeQuery()) {
            while (last5minutesRs.next()) {
                result = last5minutesRs.getDouble(USED);
            }
        }
        return result;
    }

    /**
     * Returns the total storage domain storage over all storage domains in GB.
     * @return The total storage domain value over all storage domains in GB.
     * @throws SQLException If a database issue occurs.
     */
    public Double getTotalStorageCount() throws SQLException {
        Double result = 0.0;
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement totalStoragePS = con.prepareStatement(
                     storageProperties.getProperty(TOTAL_STORAGE_COUNT));
             ResultSet totalStorageRS = totalStoragePS.executeQuery()) {
            while (totalStorageRS.next()) {
                result = totalStorageRS.getDouble(TOTAL);
            }
        }
        return result;
    }

    /**
     * Get the CPU utilization for the last 5 minutes per storage domain. Also retrieve the previous 5 minutes
     * before that, so we can calculate a trend. Usage in returned in GB. Top 10 are returned.
     * @return List of {@code TrendResources} objects containing usage of each storage domain.
     * @throws SQLException If there is a database problem.
     */
    public List<TrendResources> getStorageDomainUtilization() throws SQLException {
        List<TrendResources> result = new ArrayList<>();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement storageUtilizationPS = con.prepareStatement(
                     storageProperties.getProperty(STORAGE_DOMAIN_UTILIZATION));
             ResultSet storageUtilizationRS = storageUtilizationPS.executeQuery()) {
            while (storageUtilizationRS.next()) {
                TrendResources usage = new TrendResources();
                usage.setName(storageUtilizationRS.getString(NAME));
                usage.setUsed(storageUtilizationRS.getDouble(USED));
                usage.setTotal(storageUtilizationRS.getDouble(USED) + storageUtilizationRS.getDouble(AVAILABLE));
                usage.setPreviousUsed(storageUtilizationRS.getDouble(PREVIOUS_USED));
                result.add(usage);
            }
        }
        return result;
    }

    /**
     * Get storage utilization for the last 5 minutes per storage domain. Also retrieve the previous 5 minute
     * before that, so we can calculate a trend. Usage is returned in GB. Top 10 are returned
     * @return List of {@code TrendResources} objects containing usage of each VM.
     * @throws SQLException If there is a database problem.
     */
    public List<TrendResources> getStorageUtilizationVms() throws SQLException {
        List<TrendResources> result = new ArrayList<>();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement vmStorageUtilizationPS = con.prepareStatement(
                     storageProperties.getProperty(VM_STORAGE_UTILIZATION));
             ResultSet vmStorageUtilizationRS = vmStorageUtilizationPS.executeQuery()) {
            while (vmStorageUtilizationRS.next()) {
                TrendResources usage = new TrendResources();
                usage.setName(vmStorageUtilizationRS.getString(NAME));
                usage.setUsed(vmStorageUtilizationRS.getDouble(USED) / 1024);
                usage.setTotal(vmStorageUtilizationRS.getDouble(TOTAL) / 1024);
                usage.setPreviousUsed(vmStorageUtilizationRS.getDouble(PREVIOUS_USED)
                        / vmStorageUtilizationRS.getDouble(PREVIOUS_TOTAL) * 100);
                result.add(usage);
            }
        }
        return result;
    }
}
