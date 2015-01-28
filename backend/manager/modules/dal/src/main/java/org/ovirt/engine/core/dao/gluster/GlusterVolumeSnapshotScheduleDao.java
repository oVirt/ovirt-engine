package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DAO;
import org.ovirt.engine.core.dao.SearchDAO;

public interface GlusterVolumeSnapshotScheduleDao extends DAO, SearchDAO<GlusterVolumeSnapshotSchedule> {
    public void save(GlusterVolumeSnapshotSchedule schedule);

    public GlusterVolumeSnapshotSchedule getByVolumeId(Guid volumeId);

    public void removeByVolumeId(Guid volumeId);

    @Override
    public List<GlusterVolumeSnapshotSchedule> getAllWithQuery(String query);

    public void updateScheduleByVolumeId(Guid volumeId, GlusterVolumeSnapshotSchedule schedule);
}
