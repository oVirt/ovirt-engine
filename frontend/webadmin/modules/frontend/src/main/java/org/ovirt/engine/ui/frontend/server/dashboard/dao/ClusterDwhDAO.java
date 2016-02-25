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
import org.ovirt.engine.ui.frontend.server.dashboard.models.ClusterResourceAverage;

public class ClusterDwhDAO {
    private static final String NAME = "name"; //$NON-NLS-1$
    private static final String CPU_AVERAGE = "cpu_avg"; //$NON-NLS-1$
    private static final String MEM_AVERAGE = "mem_avg"; //$NON-NLS-1$

    private static final String CLUSTER_LAST_24_AVERAGE = "cluster.last24hours"; //$NON-NLS-1$

    private final DataSource dwhDataSource;
    private final Properties clusterProperties;

    public ClusterDwhDAO(DataSource dwhDataSource) throws DashboardDataException {
        this.dwhDataSource = dwhDataSource;
        clusterProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("ClusterDwhDAO.properties")) { //$NON-NLS-1$
            clusterProperties.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load DAO queries"); //$NON-NLS-1$
        }
    }

    public List<ClusterResourceAverage> getClusterCpuAndMemoryAverage() throws SQLException {
        List<ClusterResourceAverage> result = new ArrayList<>();
        try (Connection con = dwhDataSource.getConnection();
             PreparedStatement clusterResourcePS = con.prepareStatement(
                     clusterProperties.getProperty(CLUSTER_LAST_24_AVERAGE));
             ResultSet clusterResourceRS = clusterResourcePS.executeQuery()) {
            while (clusterResourceRS.next()) {
                result.add(new ClusterResourceAverage(clusterResourceRS.getString(NAME),
                        clusterResourceRS.getDouble(CPU_AVERAGE), clusterResourceRS.getDouble(MEM_AVERAGE)));
            }
        }
        return result;
    }


}
