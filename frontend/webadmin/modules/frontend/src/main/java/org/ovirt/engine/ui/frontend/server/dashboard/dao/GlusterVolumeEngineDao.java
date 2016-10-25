package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.InventoryStatus;

public class GlusterVolumeEngineDao extends BaseDao {

    private static final String STATUS = "status"; //$NON-NLS-1$
    private static final String BRICKS_NOT_UP = "bricks_not_up"; //$NON-NLS-1$

    private static final String GLUSTER_VOLUME_INVENTORY = "glusterVolume.inventory"; //$NON-NLS-1$

    public GlusterVolumeEngineDao(DataSource engineDataSource) throws DashboardDataException {
        super(engineDataSource, "GlusterVolumeEngineDAO.properties", GlusterVolumeEngineDao.class); //$NON-NLS-1$
    }

    public InventoryStatus getVolumeInventoryStatus() throws DashboardDataException {
        final InventoryStatus result = new InventoryStatus();

        runQuery(GLUSTER_VOLUME_INVENTORY, new QueryResultCallback() {
            @Override
            public void onResult(ResultSet rs) throws SQLException {
                processVolumeStatus(result, rs.getString(STATUS), rs.getInt(BRICKS_NOT_UP));
            }
        });

        return result;
    }

    private InventoryStatus processVolumeStatus(InventoryStatus summary, String status, int bricksNotUp) {
        summary.addCount();
        GlusterStatus volumeStatus = GlusterStatus.valueOf(status);
        if (GlusterStatus.UP == volumeStatus && bricksNotUp > 0) {
            summary.addStatus(GlusterStatus.WARNING.name().toLowerCase());
        } else {
            summary.addStatus(volumeStatus.name().toLowerCase());
        }
        return summary;
    }

}
