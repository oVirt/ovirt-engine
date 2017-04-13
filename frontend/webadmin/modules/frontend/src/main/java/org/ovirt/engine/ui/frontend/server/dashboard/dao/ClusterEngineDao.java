package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.InventoryStatus;

public class ClusterEngineDao extends BaseDao {

    private static final String COUNT = "count"; //$NON-NLS-1$

    private static final String CLUSTER_INVENTORY = "cluster.inventory"; //$NON-NLS-1$

    public ClusterEngineDao(DataSource engineDataSource) throws DashboardDataException {
        super(engineDataSource, "ClusterEngineDAO.properties", ClusterEngineDao.class); //$NON-NLS-1$
    }

    public InventoryStatus getClusterInventorySummary() throws DashboardDataException {
        final InventoryStatus result = new InventoryStatus();

        runQuery(CLUSTER_INVENTORY, rs -> result.setTotalCount(rs.getInt(COUNT)));

        return result;
    }

}
