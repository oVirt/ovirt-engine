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
import org.ovirt.engine.ui.frontend.server.dashboard.models.ResourcesTotal;
import org.ovirt.engine.ui.frontend.server.dashboard.models.TrendResources;
import org.ovirt.engine.ui.frontend.server.dashboard.models.VmStorage;

public class VmDwhDAO {
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

    private final DataSource dwhDataSource;
    private final Properties vmProperties;

    public VmDwhDAO(DataSource dwhDataSource) throws DashboardDataException {
        this.dwhDataSource = dwhDataSource;
        vmProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("VmDwhDAO.properties")) { //$NON-NLS-1$
            vmProperties.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load DAO queries"); //$NON-NLS-1$
        }
    }

    public VmStorage getVirtualStorageCount() throws SQLException {
        VmStorage result = new VmStorage();
        try (Connection con = dwhDataSource.getConnection();
                PreparedStatement vmStoragePS = con.prepareStatement(vmProperties.getProperty(VIRTUAL_STORAGE_COUNT));
                ResultSet vmStorageRS = vmStoragePS.executeQuery()) {
            while (vmStorageRS.next()) {
                result.setTotal(vmStorageRS.getDouble(TOTAL_VMS) / 1024);
                result.setUsed(vmStorageRS.getDouble(USED_VMS) / 1024);
            }
        }
        return result;
    }

    public ResourcesTotal getVirtualCpuMemCount() throws SQLException {
        ResourcesTotal result = new ResourcesTotal();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement virtualCpuMemPS = con.prepareStatement(
                     vmProperties.getProperty(VIRTUAL_CPU_MEMORY_COUNT));
             ResultSet virtualCpuMemRS = virtualCpuMemPS.executeQuery()) {
            while (virtualCpuMemRS.next()) {
                result.setCpuTotal(virtualCpuMemRS.getInt(CPU_TOTAL_VMS));
                result.setCpuUsed(virtualCpuMemRS.getInt(CPU_USED_VMS));
                result.setMemTotal(virtualCpuMemRS.getDouble(MEM_TOTAL_VMS) / 1024);
                result.setMemUsed(virtualCpuMemRS.getDouble(MEM_USED_VMS) / 1024);
            }
        }
        return result;
    }

    /**
     * Get the CPU utilization for the last 5 minutes per VM. Also retrieve the previous 5 minutes before that,
     * so we can calculate a trend. Usage in returned in percentages. Top 10 are returned.
     * @return List of {@code TrendResources} objects containing cpu usage of each vm.
     * @throws SQLException If there is a database problem.
     */
    public List<TrendResources> getCpuUtilizationVms() throws SQLException {
        List<TrendResources> result = new ArrayList<>();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement vmCpuUtilizationPS = con.prepareStatement(vmProperties.getProperty(CPU_VM_UTILIZATION));
             ResultSet utilizationRs = vmCpuUtilizationPS.executeQuery()) {
            while (utilizationRs.next()) {
                TrendResources usage = new TrendResources();
                usage.setName(utilizationRs.getString(NAME));
                usage.setUsed(utilizationRs.getDouble(CPU_USAGE_PERCENT) * utilizationRs.getDouble(NUMBER_OF_SOCKETS)
                        * utilizationRs.getDouble(VM_CPU_PER_SOCKET) / 100);
                usage.setTotal(utilizationRs.getDouble(VM_CPU_PER_SOCKET) * utilizationRs.getDouble(NUMBER_OF_SOCKETS));
                usage.setPreviousUsed(utilizationRs.getDouble(PREVIOUS_CPU_PERCENT)
                        * utilizationRs.getDouble(NUMBER_OF_SOCKETS)
                        * utilizationRs.getDouble(VM_CPU_PER_SOCKET) / 100);
                result.add(usage);
            }
        }
        return result;
    }

    /**
     * Get memory utilization for the last 5 minutes per VM. Also retrieve the previous 5 minute before that, so
     * we can calculate a trend. Memory is returned in MB. Top 10 are returned
     * @return List of {@code TrendResources} objects containing memory usage of each vm.
     * @throws SQLException If there is a database problem.
     */
    public List<TrendResources> getMemoryUtilizationVms() throws SQLException {
        List<TrendResources> result = new ArrayList<>();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement vmMemUtilizationPS = con.prepareStatement(vmProperties.getProperty(MEM_VM_UTILIZATION));
             ResultSet vmMemUtilizationRS = vmMemUtilizationPS.executeQuery()) {
            while (vmMemUtilizationRS.next()) {
                TrendResources usage = new TrendResources();
                usage.setName(vmMemUtilizationRS.getString(NAME));
                usage.setUsed(vmMemUtilizationRS.getDouble(MEMORY_USAGE_PERCENT)
                        * vmMemUtilizationRS.getDouble(MEMORY_SIZE) / 100);
                usage.setTotal(vmMemUtilizationRS.getDouble(MEMORY_SIZE));
                usage.setPreviousUsed(vmMemUtilizationRS.getDouble(PREVIOUS_MEMORY_PERCENT)
                        * vmMemUtilizationRS.getDouble(MEMORY_SIZE) / 100);
                result.add(usage);
            }
        }
        return result;
    }
}
