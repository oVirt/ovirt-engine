package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.models.ResourceUsage;
import org.ovirt.engine.ui.frontend.server.dashboard.models.ResourcesTotal;
import org.ovirt.engine.ui.frontend.server.dashboard.models.TrendResources;

public class HostDwhDao extends BaseDao {

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

    public HostDwhDao(DataSource dwhDataSource) throws DashboardDataException {
        super(dwhDataSource, "HostDwhDAO.properties", HostDwhDao.class); //$NON-NLS-1$
    }

    public List<ResourceUsage> getHourlyCpuMemUsage() throws DashboardDataException {
        final List<ResourceUsage> result = new ArrayList<>();

        runQuery(HOURLY_CPU_MEM_HISTORY, rs -> {
            ResourceUsage resourceUsage = new ResourceUsage();
            resourceUsage.setEpoch(rs.getTimestamp(DATE).getTime());
            resourceUsage.setCpuValue(rs.getDouble(CPU_USAGE));
            resourceUsage.setMemValue(rs.getDouble(MEM_USAGE));
            result.add(resourceUsage);
        });

        return result;
    }

    public ResourceUsage getLast5MinCpuMemUsage() throws DashboardDataException {
        final ResourceUsage result = new ResourceUsage();

        runQuery(LAST5_MIN_CPU_MEM_AVERAGE, rs -> {
            result.setCpuValue(rs.getDouble(CPU_USAGE));
            result.setMemValue(rs.getDouble(MEM_USAGE));
        });

        return result;
    }

    public ResourcesTotal getTotalCpuMemCount() throws DashboardDataException {
        final ResourcesTotal result = new ResourcesTotal();

        runQuery(TOTAL_CPU_MEMORY_COUNT, rs -> {
            result.setCpuTotal(rs.getInt(CPU_TOTAL));
            result.setMemTotal(rs.getDouble(MEM_TOTAL));
        });

        return result;
    }

    /**
     * Get the CPU utilization for the last 5 minutes per host. Also retrieve the previous 5 minutes before that,
     * so we can calculate a trend. Usage in returned in percentages. Top 10 are returned.
     * @return List of {@code TrendResources} objects containing cpu usage of each host.
     * @throws DashboardDataException If there is a database problem.
     */
    public List<TrendResources> getCpuUtilizationHosts() throws DashboardDataException {
        final List<TrendResources> result = new ArrayList<>();

        runQuery(CPU_HOST_UTILIZATION, rs -> {
            TrendResources usage = new TrendResources();
            usage.setName(rs.getString(NAME));
            usage.setUsed(rs.getDouble(CPU_USAGE_PERCENT));
            usage.setTotal(rs.getDouble(CORES_HOST));
            usage.setPreviousUsed(rs.getDouble(PREVIOUS_CPU_PERCENT));
            result.add(usage);
        });

        return result;
    }

    /**
     * Get memory utilization for the last 5 minutes per host. Also retrieve the previous 5 minute before that, so
     * we can calculate a trend. Memory is returned in MB. Top 10 are returned
     * @return List of {@code TrendResources} objects containing memory usage of each host.
     * @throws DashboardDataException If there is a database problem.
     */
    public List<TrendResources> getMemoryUtilizationHosts() throws DashboardDataException {
        final List<TrendResources> result = new ArrayList<>();

        runQuery(MEM_HOST_UTILIZATION, rs -> {
            TrendResources usage = new TrendResources();
            usage.setName(rs.getString(NAME));
            usage.setUsed(rs.getDouble(MEMORY_USAGE_PERCENT) * rs.getDouble(MEMORY_SIZE) / 100);
            usage.setTotal(rs.getDouble(MEMORY_SIZE));
            usage.setPreviousUsed(rs.getDouble(PREVIOUS_MEMORY_PERCENT) * rs.getDouble(MEMORY_SIZE) / 100);
            result.add(usage);
        });

        return result;
    }

}
