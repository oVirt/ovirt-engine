package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmBlockJob;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.businessentities.VmJobState;
import org.ovirt.engine.core.common.businessentities.VmJobType;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmJobDaoImpl extends MassOperationsGenericDao<VmJob, Guid> implements VmJobDao {

    public VmJobDaoImpl() {
        super("VmJobs");
    }

    @Override
    public VmJob get(Guid id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VmJob> getAll() {
        return getCallsHandler().executeReadList("GetAllVmJobs",
                vmJobRowMapper,
                getCustomMapSqlParameterSource());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("vm_job_id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmJob entity) {
        MapSqlParameterSource mapper = createIdParameterMapper(entity.getId());
        mapper.addValue("vm_id", entity.getVmId());
        mapper.addValue("job_state", entity.getJobState().getValue());
        mapper.addValue("job_type", entity.getJobType().getValue());

        if (entity.getJobType() == VmJobType.BLOCK) {
            VmBlockJob blockJob = (VmBlockJob) entity;
            mapper.addValue("block_job_type", blockJob.getBlockJobType().getValue());
            mapper.addValue("bandwidth", blockJob.getBandwidth());
            mapper.addValue("cursor_cur", blockJob.getCursorCur());
            mapper.addValue("cursor_end", blockJob.getCursorEnd());
            mapper.addValue("image_group_id", blockJob.getImageGroupId());
        } else {
            mapper.addValue("block_job_type", null);
            mapper.addValue("bandwidth", null);
            mapper.addValue("cursor_cur", null);
            mapper.addValue("cursor_end", null);
            mapper.addValue("image_group_id", null);
        }
        return mapper;
    }

    @Override
    protected RowMapper<VmJob> createEntityRowMapper() {
        return vmJobRowMapper;
    }

    private static final RowMapper<VmJob> vmJobRowMapper = (rs, rowNum) -> {
        VmJob entity;
        VmJobType jobType = VmJobType.forValue(rs.getInt("job_type"));

        switch (jobType) {
        case BLOCK:
            VmBlockJob blockJob = new VmBlockJob();
            blockJob.setBlockJobType(VmBlockJobType.forValue(rs.getInt("block_job_type")));
            blockJob.setBandwidth(rs.getLong("bandwidth"));
            blockJob.setCursorCur(rs.getLong("cursor_cur"));
            blockJob.setCursorEnd(rs.getLong("cursor_end"));
            blockJob.setImageGroupId(getGuidDefaultEmpty(rs, "image_group_id"));
            entity = blockJob;
            break;
        default:
            entity = new VmJob();
            break;
        }

        entity.setId(getGuidDefaultEmpty(rs, "vm_job_id"));
        entity.setVmId(getGuidDefaultEmpty(rs, "vm_id"));
        entity.setJobState(VmJobState.forValue(rs.getInt("job_state")));
        entity.setJobType(jobType);
        return entity;
    };
}
