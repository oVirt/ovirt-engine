package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.InventoryStatus;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.StorageStatusMap;

public class StorageDomainEngineDao extends BaseDao {

    private static final String STATUS = "status"; //$NON-NLS-1$

    private static final String STORAGE_INVENTORY = "storage.inventory"; //$NON-NLS-1$

    public StorageDomainEngineDao(DataSource engineDataSource) throws DashboardDataException {
        super(engineDataSource, "StorageDomainEngineDAO.properties", StorageDomainEngineDao.class); //$NON-NLS-1$
    }

    public InventoryStatus getStorageInventoryStatus() throws DashboardDataException {
        final InventoryStatus result = new InventoryStatus();

        runQuery(STORAGE_INVENTORY, rs -> processStorageStatus(result, rs.getInt(STATUS)));

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
