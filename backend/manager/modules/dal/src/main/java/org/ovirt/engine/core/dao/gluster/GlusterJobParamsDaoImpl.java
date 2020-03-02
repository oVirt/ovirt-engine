package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterJobParams;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class GlusterJobParamsDaoImpl extends BaseDao implements GlusterJobParamsDao {

    private static final RowMapper<GlusterJobParams> glusterJobParamsRowMapper = (rs, rowNum) -> {
        GlusterJobParams glusterJobParams = new GlusterJobParams();
        glusterJobParams.setId(getGuidDefaultEmpty(rs, "id"));
        glusterJobParams.setJobId(getGuidDefaultEmpty(rs, "job_id"));
        glusterJobParams.setParamsClassName(rs.getString("params_class_name"));
        glusterJobParams.setParamsClassValue(rs.getString("params_class_value"));

        return glusterJobParams;
    };

    @Override
    public void save(Guid jobId, List<GlusterJobParams> params) {
        for (GlusterJobParams param : params) {
            getCallsHandler().executeModification("InsertJobParams", getGlusterJobParameterSource(jobId, param));
        }
    }


    private MapSqlParameterSource getGlusterJobParameterSource(Guid jobId, GlusterJobParams param) {
        return getCustomMapSqlParameterSource()
                .addValue("id", param.getId())
                .addValue("job_id", jobId)
                .addValue("params_class_name", param.getParamsClassName())
                .addValue("params_class_value", param.getParamsClassValue());
    }

    @Override
    public List<GlusterJobParams> getJobParamsById(Guid jobId) {
        return getCallsHandler().executeReadList("GetGlusterJobParamsByJobId",
                glusterJobParamsRowMapper,
                getCustomMapSqlParameterSource().addValue("job_id", jobId));
    }

    @Override
    public void remove(Guid jobId) {
        getCallsHandler().executeModification("DeleteGlusterJobParams",
                getCustomMapSqlParameterSource().addValue("job_id", jobId));
    }
}
