package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.springframework.jdbc.core.RowMapper;

public abstract class AbstractSubjectEntityRowMapper<T extends SubjectEntity> implements RowMapper<T> {

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T se = createSubjectEntity();
        se.setEntityType(VdcObjectType.valueOf(rs.getString("entity_type")));
        se.setEntityId(BaseDao.getGuidDefaultEmpty(rs, "entity_id"));
        return se;
    }

    protected abstract T createSubjectEntity();
}
