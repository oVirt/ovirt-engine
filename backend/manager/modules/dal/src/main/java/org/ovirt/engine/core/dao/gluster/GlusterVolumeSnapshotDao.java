package org.ovirt.engine.core.dao.gluster;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DAO;
import org.ovirt.engine.core.dao.SearchDAO;

public interface GlusterVolumeSnapshotDao extends DAO, SearchDAO<GlusterVolumeSnapshotEntity> {
    public void save(GlusterVolumeSnapshotEntity snapshot);

    public void saveAll(List<GlusterVolumeSnapshotEntity> snapshots);

    public GlusterVolumeSnapshotEntity getById(Guid id);

    public GlusterVolumeSnapshotEntity getByName(Guid volumeId, String snapshotName);

    public List<GlusterVolumeSnapshotEntity> getAllByVolumeId(Guid volumeId);

    public List<GlusterVolumeSnapshotEntity> getAllByClusterId(Guid clusterId);

    @Override
    public List<GlusterVolumeSnapshotEntity> getAllWithQuery(String query);

    public void remove(Guid id);

    public void removeAll(Collection<Guid> ids);

    public void removeAllByVolumeId(Guid volumeId);

    public void removeByName(Guid volumeId, String snapshotName);

    public void updateSnapshotStatus(Guid snapshotId, GlusterSnapshotStatus status);

    public void updateSnapshotStatusByName(Guid volumeId, String snapshotName, GlusterSnapshotStatus status);

    public void updateAllInBatch(List<GlusterVolumeSnapshotEntity> snapshots);
}
