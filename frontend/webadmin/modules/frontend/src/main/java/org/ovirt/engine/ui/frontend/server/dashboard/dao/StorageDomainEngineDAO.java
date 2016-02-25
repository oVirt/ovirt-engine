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
import org.ovirt.engine.ui.frontend.server.dashboard.maps.StorageStatusMap;

public class StorageDomainEngineDAO {
    private static final String STATUS = "status"; //$NON-NLS-1$

    private static final String STORAGE_INVENTORY = "storage.inventory"; //$NON-NLS-1$

    private final DataSource engineDataSource;
    private final Properties storageProperties;

    public StorageDomainEngineDAO(DataSource engineDataSource) throws DashboardDataException {
        this.engineDataSource = engineDataSource;
        storageProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("StorageDomainEngineDAO.properties")) { //$NON-NLS-1$
            storageProperties.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load DAO queries"); //$NON-NLS-1$
        }
    }

    public InventoryStatus getStorageInventoryStatus() throws SQLException {
        InventoryStatus result = new InventoryStatus();
        try (Connection con = engineDataSource.getConnection();
                PreparedStatement storageCountPS = con.prepareStatement(
                        storageProperties.getProperty(STORAGE_INVENTORY));
                ResultSet storageCountRS = storageCountPS.executeQuery()) {
            while (storageCountRS.next()) {
                processStorageStatus(result, storageCountRS.getInt(STATUS));
            }
        }
        return result;
    }

    private InventoryStatus processStorageStatus(InventoryStatus summary, int status) {
        summary.addCount();
        if (StorageStatusMap.WARNING.isType(status)) {
            summary.addStatus(StorageStatusMap.WARNING.name().toLowerCase());
        } else if (StorageStatusMap.DOWN.isType(status)) {
            summary.addStatus(StorageStatusMap.DOWN.name().toLowerCase());
        } else {
            summary.addStatus(StorageStatusMap.UP.name().toLowerCase());
        }
        return summary;
    }
}
