package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
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

public abstract class GlusterQueriesCommandBase<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    protected GlusterVolumeDao glusterVolumeDao;

    @Inject
    protected GlusterBrickDao glusterBrickDao;

    @Inject
    protected GlusterHooksDao glusterHooksDao;

    @Inject
    protected GlusterServerServiceDao glusterServerServiceDao;

    @Inject
    protected GlusterClusterServiceDao glusterClusterServiceDao;

    @Inject
    protected GlusterGeoRepDao glusterGeoRepDao;

    @Inject
    protected GlusterVolumeSnapshotDao glusterVolumeSnapshotDao;

    @Inject
    protected GlusterVolumeSnapshotConfigDao glusterVolumeSnapshotConfigDao;

    @Inject
    protected GlusterVolumeSnapshotScheduleDao glusterVolumeSnapshotScheduleDao;

    @Inject
    protected ClusterDao clusterDao;

    @Inject
    protected GlusterUtil glusterUtil;

    public GlusterQueriesCommandBase(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    protected String getGlusterVolumeName(Guid volumeId) {
        return glusterVolumeDao.getById(volumeId).getName();
    }

    protected Guid getUpServerId(Guid clusterId) {
        VDS vds = glusterUtil.getUpServer(clusterId);
        if (vds == null) {
            throw new RuntimeException("No up server found");
        }
        return vds.getId();
    }

    protected Guid getRandomUpServerId(Guid clusterId) {
        VDS vds = glusterUtil.getRandomUpServer(clusterId);
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
