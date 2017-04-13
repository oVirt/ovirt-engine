package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.InventoryStatus;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.VmStatusMap;

public class VmEngineDao extends BaseDao {

    private static final String STATUS = "status"; //$NON-NLS-1$

    private static final String VM_INVENTORY = "vm.inventory"; //$NON-NLS-1$

    public VmEngineDao(DataSource engineDataSource) throws DashboardDataException {
        super(engineDataSource, "VmEngineDAO.properties", VmEngineDao.class); //$NON-NLS-1$
    }

    public InventoryStatus getVmInventoryStatus() throws DashboardDataException {
        final InventoryStatus result = new InventoryStatus();

        runQuery(VM_INVENTORY, rs -> processVmStatus(result, rs.getInt(STATUS)));

        return result;
    }

    private InventoryStatus processVmStatus(InventoryStatus summary, int status) {
        summary.addCount();

        if (VmStatusMap.WARNING.isType(status)) {
            summary.addStatus(VmStatusMap.WARNING.name().toLowerCase());
        } else if (VmStatusMap.DOWN.isType(status)) {
            summary.addStatus(VmStatusMap.DOWN.name().toLowerCase());
        } else {
            summary.addStatus(VmStatusMap.UP.name().toLowerCase());
        }

        return summary;
    }

}
