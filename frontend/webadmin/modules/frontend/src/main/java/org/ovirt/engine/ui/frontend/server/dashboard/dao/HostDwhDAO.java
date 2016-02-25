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
import org.ovirt.engine.ui.frontend.server.dashboard.models.ResourcesTotal;
import org.ovirt.engine.ui.frontend.server.dashboard.models.TrendResources;

public class HostDwhDAO {
    private static final String CPU = "cpu_"; //$NON-NLS-1$
    private static final String MEM = "mem_"; //$NON-NLS-1$
    private static final String DATE = "the_date"; //$NON-NLS-1$
    private static final String USAGE = "avg"; //$NON-NLS-1$
    private static final String MEM_USAGE = MEM + USAGE;
    private static final String CPU_USAGE = CPU + USAGE;
    private static final String TOTAL = "total"; //$NON-NLS-1$
    private static final String MEM_TOTAL = MEM + TOTAL;
    private static final String CPU_TOTAL = CPU + TOTAL;
    private static final String NAME="name"; //$NON-NLS-1$
    private static final String CPU_USAGE_PERCENT = "cpu_usage_percent"; //$NON-NLS-1$
    private static final String CORES_HOST = "number_of_cores"; //$NON-NLS-1$
    private static final String PREVIOUS_CPU_PERCENT = "previous_cpu_percent"; //$NON-NLS-1$
    private static final String MEMORY_USAGE_PERCENT = "memory_usage_percent"; //$NON-NLS-1$
    private static final String PREVIOUS_MEMORY_PERCENT = "previous_memory_percent"; //$NON-NLS-1$
    private static final String MEMORY_SIZE = "memory_size_mb"; //$NON-NLS-1$

    private static final String HOURLY_CPU_MEM_HISTORY = "host.hourly_cpu_mem_history"; //$NON-NLS-1$
    private static final String LAST5_MIN_CPU_MEM_AVERAGE = "host.last_5_min_cpu_mem_average"; //$NON-NLS-1$
    private static final String TOTAL_CPU_MEMORY_COUNT = "host.total_cpu_memory_count"; //$NON-NLS-1$
    private static final String CPU_HOST_UTILIZATION = "host.cpu_host_utilization"; //$NON-NLS-1$
    private static final String MEM_HOST_UTILIZATION = "host.mem_host_utilization"; //$NON-NLS-1$

    private final DataSource dwhDataSource;
    private final Properties hostProperties;

    public HostDwhDAO(DataSource dwhDataSource) throws DashboardDataException {
        this.dwhDataSource = dwhDataSource;
        hostProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("HostDwhDAO.properties")) { //$NON-NLS-1$
            hostProperties.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load DAO queries"); //$NON-NLS-1$
        }
    }

    public List<ResourceUsage> getHourlyCpuMemUsage() throws SQLException {
        List<ResourceUsage> result = new ArrayList<>();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement resourceHistoryPS = con.prepareStatement(
                     hostProperties.getProperty(HOURLY_CPU_MEM_HISTORY));
             ResultSet resourceHistoryRS = resourceHistoryPS.executeQuery()) {
            while (resourceHistoryRS.next()) {
                ResourceUsage resourceUsage = new ResourceUsage();
                resourceUsage.setEpoch(resourceHistoryRS.getTimestamp(DATE).getTime());
                resourceUsage.setCpuValue(resourceHistoryRS.getDouble(CPU_USAGE));
                resourceUsage.setMemValue(resourceHistoryRS.getDouble(MEM_USAGE));
                result.add(resourceUsage);
            }
        }
        return result;
    }

    public ResourceUsage getLast5MinCpuMemUsage() throws SQLException {
        ResourceUsage result = new ResourceUsage();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement last5minAveragePS = con.prepareStatement(
                     hostProperties.getProperty(LAST5_MIN_CPU_MEM_AVERAGE));
             ResultSet last5minAverageRS = last5minAveragePS.executeQuery()) {
            while (last5minAverageRS.next()) {
                result.setCpuValue(last5minAverageRS.getDouble(CPU_USAGE));
                result.setMemValue(last5minAverageRS.getDouble(MEM_USAGE));
            }
        }
        return result;
    }

    public ResourcesTotal getTotalCpuMemCount() throws SQLException {
        ResourcesTotal result = new ResourcesTotal();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement totalPS = con.prepareStatement(hostProperties.getProperty(TOTAL_CPU_MEMORY_COUNT));
             ResultSet totalRS = totalPS.executeQuery()) {
            while (totalRS.next()) {
                result.setCpuTotal(totalRS.getInt(CPU_TOTAL));
                result.setMemTotal(totalRS.getDouble(MEM_TOTAL));
            }
        }
        return result;
    }

    /**
     * Get the CPU utilization for the last 5 minutes per host. Also retrieve the previous 5 minutes before that,
     * so we can calculate a trend. Usage in returned in percentages. Top 10 are returned.
     * @return List of {@code TrendResources} objects containing cpu usage of each host.
     * @throws SQLException If there is a database problem.
     */
    public List<TrendResources> getCpuUtilizationHosts() throws SQLException {
        List<TrendResources> result = new ArrayList<>();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement cpuUtilizationPS = con.prepareStatement(
                     hostProperties.getProperty(CPU_HOST_UTILIZATION));
             ResultSet cpuUtilizationRS = cpuUtilizationPS.executeQuery()) {
            while (cpuUtilizationRS.next()) {
                TrendResources usage = new TrendResources();
                usage.setName(cpuUtilizationRS.getString(NAME));
                usage.setUsed(cpuUtilizationRS.getDouble(CPU_USAGE_PERCENT));
                usage.setTotal(cpuUtilizationRS.getDouble(CORES_HOST));
                usage.setPreviousUsed(cpuUtilizationRS.getDouble(PREVIOUS_CPU_PERCENT));
                result.add(usage);
            }
        }
        return result;
    }

    /**
     * Get memory utilization for the last 5 minutes per host. Also retrieve the previous 5 minute before that, so
     * we can calculate a trend. Memory is returned in MB. Top 10 are returned
     * @return List of {@code TrendResources} objects containing memory usage of each host.
     * @throws SQLException If there is a database problem.
     */
    public List<TrendResources> getMemoryUtilizationHosts() throws SQLException {
        List<TrendResources> result = new ArrayList<>();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement memUtilizationPS = con.prepareStatement(
                     hostProperties.getProperty(MEM_HOST_UTILIZATION));
             ResultSet memUtilizationRS = memUtilizationPS.executeQuery()) {
            while (memUtilizationRS.next()) {
                TrendResources usage = new TrendResources();
                usage.setName(memUtilizationRS.getString(NAME));
                usage.setUsed(memUtilizationRS.getDouble(MEMORY_USAGE_PERCENT)
                        * memUtilizationRS.getDouble(MEMORY_SIZE) / 100);
                usage.setTotal(memUtilizationRS.getDouble(MEMORY_SIZE));
                usage.setPreviousUsed(memUtilizationRS.getDouble(PREVIOUS_MEMORY_PERCENT)
                        * memUtilizationRS.getDouble(MEMORY_SIZE) / 100);
                result.add(usage);
            }
        }
        return result;
    }

}
