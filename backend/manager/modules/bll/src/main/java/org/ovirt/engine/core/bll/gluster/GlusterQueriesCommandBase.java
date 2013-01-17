package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public abstract class GlusterQueriesCommandBase<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    protected GlusterQueriesCommandBase(P parameters) {
        super(parameters);
    }

    protected GlusterVolumeDao getGlusterVolumeDao() {
        return DbFacade.getInstance()
                .getGlusterVolumeDao();
    }

    protected GlusterBrickDao getGlusterBrickDao() {
        return DbFacade.getInstance().getGlusterBrickDao();
    }

    protected String getGlusterVolumeName(Guid volumeId) {
        return getGlusterVolumeDao().getById(volumeId).getName();
    }

    protected Guid getUpServerId(Guid clusterId) {
        VDS vds = getClusterUtils().getUpServer(clusterId);
        if (vds == null) {
            throw new RuntimeException("No up server found");
        }
        return vds.getId();
    }

    protected ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    protected VDSBrokerFrontend getBackendResourceManager() {
        return getBackend().getResourceManager();
    }
}
