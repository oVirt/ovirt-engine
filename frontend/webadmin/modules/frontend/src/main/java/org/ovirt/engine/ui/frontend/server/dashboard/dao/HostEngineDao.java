package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.InventoryStatus;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.HostStatusMap;

public class HostEngineDao extends BaseDao {

    private static final String STATUS = "status"; //$NON-NLS-1$

    private static final String HOST_INVENTORY = "host.inventory"; //$NON-NLS-1$

    public HostEngineDao(DataSource engineDataSource) throws DashboardDataException {
        super(engineDataSource, "HostEngineDAO.properties", HostEngineDao.class); //$NON-NLS-1$
    }

    public InventoryStatus getHostInventoryStatus() throws DashboardDataException {
        final InventoryStatus result = new InventoryStatus();

        runQuery(HOST_INVENTORY, rs -> processHostStatus(result, rs.getInt(STATUS)));

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
