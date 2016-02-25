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
import org.ovirt.engine.ui.frontend.server.dashboard.maps.HostStatusMap;

public class HostEngineDAO {
    private static final String STATUS = "status"; //$NON-NLS-1$

    private static final String HOST_INVENTORY = "host.inventory"; //$NON-NLS-1$

    private final DataSource engineDataSource;
    private final Properties hostProperties;

    public HostEngineDAO(DataSource engineDataSource) throws DashboardDataException {
        this.engineDataSource = engineDataSource;
        hostProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("HostEngineDAO.properties")) { //$NON-NLS-1$
            hostProperties.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load DAO queries"); //$NON-NLS-1$
        }
    }

    public InventoryStatus getHostInventoryStatus() throws SQLException {
        InventoryStatus result = new InventoryStatus();
        try (Connection con = engineDataSource.getConnection();
                PreparedStatement cpuSummary = con.prepareStatement(hostProperties.getProperty(HOST_INVENTORY));
                ResultSet rs = cpuSummary.executeQuery()) {
            while (rs.next()) {
                processHostStatus(result, rs.getInt(STATUS));
            }
        }
        return result;
    }

    private InventoryStatus processHostStatus(InventoryStatus summary, int status) {
        summary.addCount();
        if (HostStatusMap.WARNING.isType(status)) {
            summary.addStatus(HostStatusMap.WARNING.name().toLowerCase());
        } else if (HostStatusMap.DOWN.isType(status)) {
            summary.addStatus(HostStatusMap.DOWN.name().toLowerCase());
        } else {
            summary.addStatus(HostStatusMap.UP.name().toLowerCase());
        }
        return summary;
    }

}
