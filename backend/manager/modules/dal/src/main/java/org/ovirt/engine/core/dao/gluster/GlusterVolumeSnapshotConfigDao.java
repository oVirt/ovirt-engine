package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;
import org.ovirt.engine.core.dao.SearchDao;

public interface GlusterVolumeSnapshotConfigDao extends Dao, SearchDao<GlusterVolumeSnapshotConfig> {
    public void save(GlusterVolumeSnapshotConfig entity);

    public List<GlusterVolumeSnapshotConfig> getConfigByClusterId(Guid clusterId);

    public List<GlusterVolumeSnapshotConfig> getConfigByVolumeId(Guid clusterId, Guid volumeId);

    public GlusterVolumeSnapshotConfig getConfigByClusterIdAndName(Guid clusterId,
            String paramName);

    public GlusterVolumeSnapshotConfig getConfigByVolumeIdAndName(Guid clusterId,
            Guid volumeId,
            String paramName);

    @Override
    public List<GlusterVolumeSnapshotConfig> getAllWithQuery(String query);

    public void updateConfigByClusterIdAndName(Guid clusterId, String paramName, String paramValue);

    public void updateConfigByVolumeIdAndName(Guid clusterId, Guid volumeId, String paramName, String paramValue);
}
