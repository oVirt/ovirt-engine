package org.ovirt.engine.core.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Implements the CRUD operations for {@code JobSubjectEntity}, a satellite table of {@code Job}.
 */
@Named
@Singleton
public class JobSubjectEntityDaoImpl extends BaseDao implements JobSubjectEntityDao {

    private static JobSubjectEntityRowMapper jobSubjectEntityRowMapper = new JobSubjectEntityRowMapper();

    @Override
    public void save(Guid jobId, Guid entityId, VdcObjectType entityType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("job_id", jobId)
                .addValue("entity_id", entityId)
                .addValue("entity_type", EnumUtils.nameOrNull(entityType));

        getCallsHandler().executeModification("InsertJobSubjectEntity", parameterSource);
    }

    @Override
    public Map<Guid, VdcObjectType> getJobSubjectEntityByJobId(Guid jobId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("job_id", jobId);

        List<SubjectEntity> list =
                getCallsHandler().executeReadList("GetJobSubjectEntityByJobId",
                        jobSubjectEntityRowMapper,
                        parameterSource);

        Map<Guid, VdcObjectType> entityMap = new HashMap<>();
        for (SubjectEntity jobSubjectEntity : list) {
            entityMap.put(jobSubjectEntity.getEntityId(), jobSubjectEntity.getEntityType());
        }
        return entityMap;
    }

    @Override
    public List<Guid> getJobIdByEntityId(Guid entityId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("entity_id", entityId);

        return getCallsHandler().executeReadList("GetAllJobIdsByEntityId", createGuidMapper(), parameterSource);
    }

    private static class JobSubjectEntityRowMapper extends AbstractSubjectEntityRowMapper<SubjectEntity> {

        protected SubjectEntity createSubjectEntity() {
            return new SubjectEntity();
        }
    }
}
