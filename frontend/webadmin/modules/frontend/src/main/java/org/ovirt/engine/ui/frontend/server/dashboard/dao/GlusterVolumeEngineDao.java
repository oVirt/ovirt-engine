package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.InventoryStatus;
import org.ovirt.engine.ui.frontend.server.dashboard.VDOVolumeDetails;

public class GlusterVolumeEngineDao extends BaseDao {

    private static final String STATUS = "status"; //$NON-NLS-1$
    private static final String BRICKS_NOT_UP = "bricks_not_up"; //$NON-NLS-1$
    private static final String VDO_SAVINGS = "vdo_savings";//$NON-NLS-1$
    private static final String VOLUME_NAME = "vol_name";//$NON-NLS-1$

    private static final String GLUSTER_VOLUME_INVENTORY = "glusterVolume.inventory"; //$NON-NLS-1$
    private static final String GLUSTER_VOLUME_VDO_SAVINGS = "glusterVolume.vdoSavings"; //$NON-NLS-1$

    public GlusterVolumeEngineDao(DataSource engineDataSource) throws DashboardDataException {
        super(engineDataSource, "GlusterVolumeEngineDAO.properties", GlusterVolumeEngineDao.class); //$NON-NLS-1$
    }

    public InventoryStatus getVolumeInventoryStatus() throws DashboardDataException {
        final InventoryStatus result = new InventoryStatus();

        runQuery(GLUSTER_VOLUME_INVENTORY,
                rs -> processVolumeStatus(result, rs.getString(STATUS), rs.getInt(BRICKS_NOT_UP)));

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

    public List<VDOVolumeDetails> getVdoVolumesSavingsList() throws DashboardDataException {
        final List<VDOVolumeDetails> vdoSavingsVolumeList = new ArrayList<>();
        runQuery(GLUSTER_VOLUME_VDO_SAVINGS,
                rs -> {
                    VDOVolumeDetails vdo = new VDOVolumeDetails();
                    vdo.setVolumeName(rs.getString(VOLUME_NAME));
                    vdo.setVdoSavings(rs.getInt(VDO_SAVINGS));
                    vdoSavingsVolumeList.add(vdo);
                });

        return vdoSavingsVolumeList;
    }

}
