package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.InventoryStatus;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.DcStatusMap;

public class DataCenterDao extends BaseDao {

    private static final String STATUS = "status"; //$NON-NLS-1$

    private static final String DC_INVENTORY = "datacenter.inventory"; //$NON-NLS-1$

    public DataCenterDao(DataSource engineDataSource) throws DashboardDataException {
        super(engineDataSource, "DataCenterDAO.properties", DataCenterDao.class); //$NON-NLS-1$
    }

    public InventoryStatus getDcInventoryStatus() throws DashboardDataException {
        final InventoryStatus result = new InventoryStatus();

        runQuery(DC_INVENTORY, rs -> processDcStatus(result, rs.getInt(STATUS)));

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
