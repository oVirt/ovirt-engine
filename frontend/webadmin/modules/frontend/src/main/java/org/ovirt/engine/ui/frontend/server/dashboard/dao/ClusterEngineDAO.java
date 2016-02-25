package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.InventoryStatus;

public class ClusterEngineDAO {
    private static final String COUNT = "count"; //$NON-NLS-1$

    private static final String CLUSTER_INVENTORY = "cluster.inventory"; //$NON-NLS-1$

    private final DataSource engineDataSource;
    private final Properties clusterProperties;

    public ClusterEngineDAO(DataSource engineDataSource) throws DashboardDataException {
        this .engineDataSource = engineDataSource;
        clusterProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("ClusterEngineDAO.properties")) { //$NON-NLS-1$
            clusterProperties.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load DAO queries"); //$NON-NLS-1$
        }
    }

    public InventoryStatus getClusterInventorySummary() throws SQLException {
        InventoryStatus result = new InventoryStatus();
        try (Connection con = engineDataSource.getConnection();
             PreparedStatement clusterCountPS = con.prepareStatement(clusterProperties.getProperty(CLUSTER_INVENTORY));
             ResultSet clusterCountRS = clusterCountPS.executeQuery()) {
            while (clusterCountRS.next()) {
                result.setTotalCount(clusterCountRS.getInt(COUNT));
            }
        }
        return result;
    }
}
