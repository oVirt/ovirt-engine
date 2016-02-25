package org.ovirt.engine.ui.frontend.server.dashboard;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.dao.ClusterEngineDAO;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.DataCenterDAO;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.HostEngineDAO;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.StorageDomainEngineDAO;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.VmEngineDAO;

public class InventoryHelper {
    /**
     * Get the status information for the data centers.
     * @param engineDataSource The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts mapped from the original status to
     * the Up/Down/Error status.
     * @throws SQLException when there is an issue with the query or database.
     * @throws DashboardDataException If there is a problem reading the query properties
     */
    public static InventoryStatus getDcInventoryStatus(DataSource engineDataSource) throws SQLException,
            DashboardDataException {
        DataCenterDAO dao = new DataCenterDAO(engineDataSource);
        return dao.getDcInventoryStatus();
    }

    /**
     * Get the status information for the clusters. Since there are no cluster statuses, they are always 'UP'
     * @param engineDataSource The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts.
     * @throws SQLException when there is an issue with the query or database.
     * @throws DashboardDataException If there is a problem reading the query properties
     */
    public static InventoryStatus getClusterInventoryStatus(DataSource engineDataSource) throws SQLException,
            DashboardDataException {
        ClusterEngineDAO dao = new ClusterEngineDAO(engineDataSource);
        return dao.getClusterInventorySummary();
    }

    /**
     * Get the status information for the hosts.
     * @param engineDataSource The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts mapped from the original status to
     * the Up/Down/Error status.
     * @throws SQLException when there is an issue with the query or database.
     * @throws DashboardDataException If there is a problem reading the query properties
     */
    public static InventoryStatus getHostInventoryStatus(DataSource engineDataSource) throws SQLException,
            DashboardDataException {
        HostEngineDAO dao = new HostEngineDAO(engineDataSource);
        return dao.getHostInventoryStatus();
    }

    /**
     * Get the status information for the storage domains.
     * @param engineDataSource The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts mapped from the original status to
     * the Up/Down/Error status.
     * @throws SQLException when there is an issue with the query or database.
     * @throws DashboardDataException If there is a problem reading the query properties
     */
    public static InventoryStatus getStorageInventoryStatus(DataSource engineDataSource) throws SQLException,
            DashboardDataException {
        StorageDomainEngineDAO dao = new StorageDomainEngineDAO(engineDataSource);
        return dao.getStorageInventoryStatus();
    }

    /**
     * Get the status information for the VMs.
     * @param engineDataSource The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts mapped from the original status to
     * the Up/Down/Error status.
     * @throws SQLException when there is an issue with the query or database.
     * @throws DashboardDataException If there is a problem reading the query properties
     */
    public static InventoryStatus getVmInventorySummary(DataSource engineDataSource) throws SQLException,
            DashboardDataException {
        VmEngineDAO dao = new VmEngineDAO(engineDataSource);
        return dao.getVmInventoryStatus();
    }
}
