package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.models.ClusterResourceAverage;

public class ClusterDwhDao extends BaseDao {

    private static final String NAME = "name"; //$NON-NLS-1$
    private static final String CPU_AVERAGE = "cpu_avg"; //$NON-NLS-1$
    private static final String MEM_AVERAGE = "mem_avg"; //$NON-NLS-1$

    private static final String CLUSTER_LAST_24_AVERAGE = "cluster.last24hours"; //$NON-NLS-1$

    public ClusterDwhDao(DataSource dwhDataSource) throws DashboardDataException {
        super(dwhDataSource, "ClusterDwhDAO.properties", ClusterDwhDao.class); //$NON-NLS-1$
    }

    public List<ClusterResourceAverage> getClusterCpuAndMemoryAverage() throws DashboardDataException {
        final List<ClusterResourceAverage> result = new ArrayList<>();

        runQuery(CLUSTER_LAST_24_AVERAGE, rs -> result.add(new ClusterResourceAverage(rs.getString(NAME),
                rs.getDouble(CPU_AVERAGE),
                rs.getDouble(MEM_AVERAGE))));

        return result;
    }

}
