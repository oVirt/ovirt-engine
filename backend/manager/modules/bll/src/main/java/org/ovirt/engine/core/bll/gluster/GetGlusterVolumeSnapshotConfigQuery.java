package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeQueriesParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class GetGlusterVolumeSnapshotConfigQuery<P extends GlusterVolumeQueriesParameters> extends GlusterQueriesCommandBase<P> {
    public GetGlusterVolumeSnapshotConfigQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    private GlusterSnapshotSyncJob glusterSnapshotSyncJob;

    private Pair<List<GlusterVolumeSnapshotConfig>, List<GlusterVolumeSnapshotConfig>> getConfigPair(List<GlusterVolumeSnapshotConfig> configs) {
        List<GlusterVolumeSnapshotConfig> clusterCfgs = new ArrayList<>();
        List<GlusterVolumeSnapshotConfig> volumeCfgs = new ArrayList<>();

        for (GlusterVolumeSnapshotConfig config : configs) {
            if (Guid.isNullOrEmpty(config.getVolumeId())) {
                clusterCfgs.add(config);
            } else if (getParameters().getVolumeId() != null
                    && config.getVolumeId().equals(getParameters().getVolumeId())) {
                volumeCfgs.add(config);
            }
        }

        return new Pair<>(clusterCfgs, volumeCfgs);
    }

    @Override
    public void executeQueryCommand() {
        List<GlusterVolumeSnapshotConfig> configs =
                glusterVolumeSnapshotConfigDao.getConfigByClusterId(getParameters().getClusterId());

        if (configs != null && configs.size() > 0) {
            getQueryReturnValue().setReturnValue(getConfigPair(configs));
        } else {
            glusterSnapshotSyncJob.refreshSnapshotConfigInCluster(clusterDao.get(getParameters().getClusterId()));
            // fetch the configuration again after sync
            configs = glusterVolumeSnapshotConfigDao.getConfigByClusterId(getParameters().getClusterId());
            getQueryReturnValue().setReturnValue(getConfigPair(configs));
        }
    }
}
