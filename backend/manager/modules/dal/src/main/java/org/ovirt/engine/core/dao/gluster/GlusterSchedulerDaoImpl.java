package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterJobDetails;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class GlusterSchedulerDaoImpl extends BaseDao implements GlusterSchedulerDao {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final RowMapper<GlusterJobDetails> glusterJobDetailsRowMapper = (rs, rowNum) -> {
        GlusterJobDetails glusterJobDetails = new GlusterJobDetails();
        glusterJobDetails.setJobId(getGuidDefaultEmpty(rs, "job_id"));
        glusterJobDetails.setJobName(rs.getString("job_name"));
        glusterJobDetails.setJobClassName(rs.getString("job_class_name"));
        glusterJobDetails.setCronSchedule(rs.getString("cron_schedule"));
        glusterJobDetails.setTimeZone(rs.getString("timezone"));

        return glusterJobDetails;
    };

    @Override
    public GlusterJobDetails getGlusterJobById(Guid jobId) {
        return getCallsHandler().executeRead("GetGlusterJobById",
                glusterJobDetailsRowMapper,
                getCustomMapSqlParameterSource().addValue("job_id", jobId));
    }

    @Override
    public List<GlusterJobDetails> getAllJobs() {
        return getCallsHandler().executeReadList("GetAllGlusterSchedulerJobs",
                glusterJobDetailsRowMapper,
                getCustomMapSqlParameterSource());
    }

    @Override
    public void save(GlusterJobDetails job) {
        getCallsHandler().executeModification("InsertSchedulerJob", getGlusterJobParameterSource(job));
    }

    private MapSqlParameterSource getGlusterJobParameterSource(GlusterJobDetails job) {
        return getCustomMapSqlParameterSource()
                .addValue("job_id", job.getJobId())
                .addValue("job_name", job.getJobName())
                .addValue("job_class_name", job.getJobClassName())
                .addValue("cron_schedule", job.getCronSchedule())
                .addValue("start_date", job.getStartDate())
                .addValue("end_date", job.getEndDate())
                .addValue("timezone", job.getTimeZone());
    }

    @Override
    public void remove(Guid jobId) {
        getCallsHandler().executeModification("DeleteGlusterJob",
                getCustomMapSqlParameterSource().addValue("job_id", jobId));

    }

}
