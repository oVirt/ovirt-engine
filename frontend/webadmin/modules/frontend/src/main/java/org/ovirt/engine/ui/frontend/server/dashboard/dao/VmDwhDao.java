package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.models.ResourcesTotal;
import org.ovirt.engine.ui.frontend.server.dashboard.models.TrendResources;
import org.ovirt.engine.ui.frontend.server.dashboard.models.VmStorage;

public class VmDwhDao extends BaseDao {

    private static final String NAME="name"; //$NON-NLS-1$
    private static final String TOTAL_VMS = "total_vms"; //$NON-NLS-1$
    private static final String USED_VMS = "used_vms"; //$NON-NLS-1$
    private static final String CPU = "cpu_"; //$NON-NLS-1$
    private static final String MEM = "mem_"; //$NON-NLS-1$
    private static final String MEM_TOTAL_VMS = MEM + TOTAL_VMS;
    private static final String MEM_USED_VMS = MEM + USED_VMS;
    private static final String CPU_TOTAL_VMS = CPU + TOTAL_VMS;
    private static final String CPU_USED_VMS = CPU + USED_VMS;
    private static final String VM_CPU_PER_SOCKET = "cpu_per_socket"; //$NON-NLS-1$
    private static final String NUMBER_OF_SOCKETS = "number_of_sockets"; //$NON-NLS-1$
    private static final String CPU_USAGE_PERCENT = "cpu_usage_percent"; //$NON-NLS-1$
    private static final String PREVIOUS_CPU_PERCENT = "previous_cpu_percent"; //$NON-NLS-1$
    private static final String MEMORY_USAGE_PERCENT = "memory_usage_percent"; //$NON-NLS-1$
    private static final String PREVIOUS_MEMORY_PERCENT = "previous_memory_percent"; //$NON-NLS-1$
    private static final String MEMORY_SIZE = "memory_size_mb"; //$NON-NLS-1$

    private static final String VIRTUAL_STORAGE_COUNT = "vm.virtual_storage_count"; //$NON-NLS-1$
    private static final String VIRTUAL_CPU_MEMORY_COUNT = "vm.virtual_cpu_memory_count"; //$NON-NLS-1$
    private static final String CPU_VM_UTILIZATION = "vm.cpu_utilization"; //$NON-NLS-1$
    private static final String MEM_VM_UTILIZATION = "vm.mem_utilization"; //$NON-NLS-1$

    public VmDwhDao(DataSource dwhDataSource) throws DashboardDataException {
        super(dwhDataSource, "VmDwhDAO.properties", VmDwhDao.class); //$NON-NLS-1$
    }

    public VmStorage getVirtualStorageCount() throws DashboardDataException {
        final VmStorage result = new VmStorage();

        runQuery(VIRTUAL_STORAGE_COUNT, rs -> {
            result.setTotal(rs.getDouble(TOTAL_VMS) / 1024);
            result.setUsed(rs.getDouble(USED_VMS) / 1024);
        });

        return result;
    }

    public ResourcesTotal getVirtualCpuMemCount() throws DashboardDataException {
        final ResourcesTotal result = new ResourcesTotal();

        runQuery(VIRTUAL_CPU_MEMORY_COUNT, rs -> {
            result.setCpuTotal(rs.getInt(CPU_TOTAL_VMS));
            result.setCpuUsed(rs.getInt(CPU_USED_VMS));
            result.setMemTotal(rs.getDouble(MEM_TOTAL_VMS) / 1024);
            result.setMemUsed(rs.getDouble(MEM_USED_VMS) / 1024);
        });

        return result;
    }

    /**
     * Get the CPU utilization for the last 5 minutes per VM. Also retrieve the previous 5 minutes before that,
     * so we can calculate a trend. Usage in returned in percentages. Top 10 are returned.
     * @return List of {@code TrendResources} objects containing cpu usage of each vm.
     * @throws DashboardDataException If there is a database problem.
     */
    public List<TrendResources> getCpuUtilizationVms() throws DashboardDataException {
        final List<TrendResources> result = new ArrayList<>();

        runQuery(CPU_VM_UTILIZATION, rs -> {
            TrendResources usage = new TrendResources();
            usage.setName(rs.getString(NAME));
            usage.setUsed(rs.getDouble(CPU_USAGE_PERCENT) * rs.getDouble(NUMBER_OF_SOCKETS)
                    * rs.getDouble(VM_CPU_PER_SOCKET) / 100);
            usage.setTotal(rs.getDouble(VM_CPU_PER_SOCKET) * rs.getDouble(NUMBER_OF_SOCKETS));
            usage.setPreviousUsed(rs.getDouble(PREVIOUS_CPU_PERCENT)
                    * rs.getDouble(NUMBER_OF_SOCKETS)
                    * rs.getDouble(VM_CPU_PER_SOCKET) / 100);
            result.add(usage);
        });

        return result;
    }

    /**
     * Get memory utilization for the last 5 minutes per VM. Also retrieve the previous 5 minute before that, so
     * we can calculate a trend. Memory is returned in MB. Top 10 are returned
     * @return List of {@code TrendResources} objects containing memory usage of each vm.
     * @throws DashboardDataException If there is a database problem.
     */
    public List<TrendResources> getMemoryUtilizationVms() throws DashboardDataException {
        final List<TrendResources> result = new ArrayList<>();

        runQuery(MEM_VM_UTILIZATION, rs -> {
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
