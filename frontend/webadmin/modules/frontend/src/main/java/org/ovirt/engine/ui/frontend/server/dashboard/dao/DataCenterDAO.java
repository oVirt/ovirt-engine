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
import org.ovirt.engine.ui.frontend.server.dashboard.maps.DcStatusMap;

public class DataCenterDAO {
    private static final String STATUS = "status"; //$NON-NLS-1$

    private static final String DC_INVENTORY = "datacenter.inventory"; //$NON-NLS-1$

    final DataSource engineDataSource;
    private final Properties datacenterProperties;

    public DataCenterDAO(DataSource engineDataSource) throws DashboardDataException {
        this.engineDataSource = engineDataSource;
        datacenterProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("DataCenterDAO.properties")) { //$NON-NLS-1$
            datacenterProperties.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load DAO queries"); //$NON-NLS-1$
        }
    }

    public InventoryStatus getDcInventoryStatus() throws SQLException {
        InventoryStatus result = new InventoryStatus();
        try (Connection con = engineDataSource.getConnection();
             PreparedStatement dcStatusPS = con.prepareStatement(datacenterProperties.getProperty(DC_INVENTORY));
             ResultSet dcStatusRS = dcStatusPS.executeQuery()) {
            while (dcStatusRS.next()) {
                processDcStatus(result, dcStatusRS.getInt(STATUS));
            }
        }
        return result;
    }

    private InventoryStatus processDcStatus(InventoryStatus summary, int status) {
        summary.addCount();
        if (DcStatusMap.WARNING.isType(status)) {
            summary.addStatus(DcStatusMap.WARNING.name().toLowerCase());
        } else if (DcStatusMap.DOWN.isType(status)) {
            summary.addStatus(DcStatusMap.DOWN.name().toLowerCase());
        } else {
            summary.addStatus(DcStatusMap.UP.name().toLowerCase());
        }
        return summary;
    }

}
