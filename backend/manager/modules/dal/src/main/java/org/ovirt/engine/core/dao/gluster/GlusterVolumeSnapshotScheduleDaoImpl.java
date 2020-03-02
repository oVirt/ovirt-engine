package org.ovirt.engine.core.dao.gluster;

import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class GlusterVolumeSnapshotScheduleDaoImpl extends BaseDao implements GlusterVolumeSnapshotScheduleDao {

    private static final RowMapper<GlusterVolumeSnapshotSchedule> snapshotScheduleRowMapper = (rs, rowNum) -> {
        GlusterVolumeSnapshotSchedule schedule = new GlusterVolumeSnapshotSchedule();

        schedule.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
        schedule.setVolumeId(getGuidDefaultEmpty(rs, "volume_id"));
        schedule.setJobId(Guid.createGuidFromString(rs.getString("job_id")));
        schedule.setSnapshotNamePrefix(rs.getString("snapshot_name_prefix"));
        schedule.setSnapshotDescription(rs.getString("snapshot_description"));
        schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.from(rs.getString("recurrence")));
        schedule.setTimeZone(rs.getString("time_zone"));
        schedule.setInterval(rs.getInt("interval"));
        schedule.setStartDate(rs.getTimestamp("start_date") == null ? null : new Date(rs.getTimestamp("start_date")
                .getTime()));
        schedule.setExecutionTime(rs.getTime("execution_time"));
        schedule.setDays(rs.getString("days"));
        schedule.setEndByDate(rs.getTimestamp("end_by"));

        return schedule;
    };

    @Override
    public void save(GlusterVolumeSnapshotSchedule schedule) {
        getCallsHandler().executeModification("InsertGlusterVolumeSnapshotSchedule",
                createFullParameterMapper(schedule));
    }

    @Override
    public GlusterVolumeSnapshotSchedule getByVolumeId(Guid volumeId) {
        return getCallsHandler().executeRead("GetGlusterVolumeSnapshotScheduleByVolumeId", snapshotScheduleRowMapper,
                getCustomMapSqlParameterSource().addValue("volume_id", volumeId));
    }

    @Override
    public void removeByVolumeId(Guid volumeId) {
        getCallsHandler().executeModification("DeleteGlusterVolumeSnapshotScheduleByVolumeId",
                getCustomMapSqlParameterSource().addValue("volume_id", volumeId));
    }

    @Override
    public List<GlusterVolumeSnapshotSchedule> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, snapshotScheduleRowMapper);
    }

    @Override
    public void updateScheduleByVolumeId(Guid volumeId, GlusterVolumeSnapshotSchedule schedule) {
        getCallsHandler().executeModification("UpdateGlusterVolumeSnapshotScheduleByVolumeId",
                createFullParameterMapper(schedule));
    }

    protected MapSqlParameterSource createFullParameterMapper(GlusterVolumeSnapshotSchedule schedule) {
        return getCustomMapSqlParameterSource().addValue("volume_id", schedule.getVolumeId())
                .addValue("job_id", schedule.getJobId())
                .addValue("snapshot_name_prefix", schedule.getSnapshotNamePrefix())
                .addValue("snapshot_description", schedule.getSnapshotDescription())
                .addValue("recurrence", schedule.getRecurrence().toString())
                .addValue("time_zone", schedule.getTimeZone())
                .addValue("interval", schedule.getInterval())
                .addValue("start_date", schedule.getStartDate())
                .addValue("execution_time", schedule.getExecutionTime())
                .addValue("days", schedule.getDays())
                .addValue("end_by", schedule.getEndByDate());
    }
}
