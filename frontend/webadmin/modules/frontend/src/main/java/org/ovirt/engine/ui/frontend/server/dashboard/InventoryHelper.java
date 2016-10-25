package org.ovirt.engine.ui.frontend.server.dashboard;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.dao.ClusterEngineDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.DataCenterDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.GlusterVolumeEngineDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.HostEngineDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.StorageDomainEngineDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.VmEngineDao;

public class InventoryHelper {
    /**
     * Get the status information for the data centers.
     * @param engineDataSource The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts mapped from the original status to
     * the Up/Down/Error status.
     * @throws DashboardDataException If there is a problem reading the query properties
     */
    public static InventoryStatus getDcInventoryStatus(DataSource engineDataSource) throws DashboardDataException {
        DataCenterDao dao = new DataCenterDao(engineDataSource);
        return dao.getDcInventoryStatus();
    }

    /**
     * Get the status information for the clusters. Since there are no cluster statuses, they are always 'UP'
     * @param engineDataSource The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts.
     * @throws DashboardDataException If there is a problem reading the query properties
     */
    public static InventoryStatus getClusterInventoryStatus(DataSource engineDataSource) throws DashboardDataException {
        ClusterEngineDao dao = new ClusterEngineDao(engineDataSource);
        return dao.getClusterInventorySummary();
    }

    /**
     * Get the status information for the hosts.
     * @param engineDataSource The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts mapped from the original status to
     * the Up/Down/Error status.
     * @throws DashboardDataException If there is a problem reading the query properties
     */
    public static InventoryStatus getHostInventoryStatus(DataSource engineDataSource) throws DashboardDataException {
        HostEngineDao dao = new HostEngineDao(engineDataSource);
        return dao.getHostInventoryStatus();
    }

    /**
     * Get the status information for the storage domains.
     * @param engineDataSource The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts mapped from the original status to
     * the Up/Down/Error status.
     * @throws DashboardDataException If there is a problem reading the query properties
     */
    public static InventoryStatus getStorageInventoryStatus(DataSource engineDataSource) throws DashboardDataException {
        StorageDomainEngineDao dao = new StorageDomainEngineDao(engineDataSource);
        return dao.getStorageInventoryStatus();
    }

    /**
     * Get the status information for the VMs.
     * @param engineDataSource The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts mapped from the original status to
     * the Up/Down/Error status.
     * @throws DashboardDataException If there is a problem reading the query properties
     */
    public static InventoryStatus getVmInventorySummary(DataSource engineDataSource) throws DashboardDataException {
        VmEngineDao dao = new VmEngineDao(engineDataSource);
        return dao.getVmInventoryStatus();
    }

    /**
     * Get the status information for Gluster Volumes.
     * @param engineDataSource
     *            The data source to use.
     * @return An {@code InventoryStatus} object containing the status counts mapped from the original status to the
     *         Up/Down/Warning status.
     * @throws DashboardDataException
     *             If there is a problem reading the query properties
     */
    public static InventoryStatus getGlusterVolumeInventorySummary(DataSource engineDataSource)
            throws DashboardDataException {
        GlusterVolumeEngineDao dao = new GlusterVolumeEngineDao(engineDataSource);
        return dao.getVolumeInventoryStatus();
    }

}
