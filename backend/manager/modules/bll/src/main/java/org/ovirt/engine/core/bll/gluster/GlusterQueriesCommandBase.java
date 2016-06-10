package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterClusterServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotConfigDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotScheduleDao;

public abstract class GlusterQueriesCommandBase<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    protected GlusterQueriesCommandBase(P parameters) {
        super(parameters);
    }

    public GlusterQueriesCommandBase(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    protected GlusterVolumeDao getGlusterVolumeDao() {
        return DbFacade.getInstance()
                .getGlusterVolumeDao();
    }

    protected GlusterBrickDao getGlusterBrickDao() {
        return DbFacade.getInstance().getGlusterBrickDao();
    }

    protected GlusterHooksDao getGlusterHookDao() {
        return DbFacade.getInstance().getGlusterHooksDao();
    }

    protected GlusterServerServiceDao getGlusterServerServiceDao() {
        return DbFacade.getInstance().getGlusterServerServiceDao();
    }

    protected GlusterClusterServiceDao getGlusterClusterServiceDao() {
        return DbFacade.getInstance().getGlusterClusterServiceDao();
    }

    protected String getGlusterVolumeName(Guid volumeId) {
        return getGlusterVolumeDao().getById(volumeId).getName();
    }

    protected GlusterUtil getGlusterUtils() {
        return GlusterUtil.getInstance();
    }

    protected GlusterGeoRepDao getGeoRepDao() {
        return DbFacade.getInstance().getGlusterGeoRepDao();
    }

    protected GlusterVolumeSnapshotDao getGlusterVolumeSnapshotDao() {
        return DbFacade.getInstance().getGlusterVolumeSnapshotDao();
    }

    protected GlusterVolumeSnapshotConfigDao getGlusterVolumeSnapshotConfigDao() {
        return DbFacade.getInstance().getGlusterVolumeSnapshotConfigDao();
    }

    protected GlusterVolumeSnapshotScheduleDao getGlusterVolumeSnapshotScheduleDao() {
        return DbFacade.getInstance().getGlusterVolumeSnapshotScheduleDao();
    }

    protected Guid getUpServerId(Guid clusterId) {
        VDS vds = getGlusterUtils().getUpServer(clusterId);
        if (vds == null) {
            throw new RuntimeException("No up server found");
        }
        return vds.getId();
    }

    protected ClusterDao getClusterDao() {
        return DbFacade.getInstance().getClusterDao();
    }

    protected Guid getRandomUpServerId(Guid clusterId) {
        VDS vds = getGlusterUtils().getRandomUpServer(clusterId);
        if (vds == null) {
            throw new RuntimeException("No up server found");
        }
        return vds.getId();
    }

    @Override
    protected VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters)
            throws EngineException {
        VDSReturnValue returnValue = super.runVdsCommand(commandType, parameters);
        if (!returnValue.getSucceeded()) {
            throw new EngineException(returnValue.getVdsError().getCode(), returnValue.getVdsError()
                    .getMessage());
        }
        return returnValue;
    }
}
