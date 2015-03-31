package org.ovirt.engine.core.dao.profiles;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public abstract class ProfileBaseDaoFacadeImpl<T extends ProfileBase> extends DefaultGenericDaoDbFacade<T, Guid> implements ProfilesDao<T> {
    protected final RowMapper<T> mapper = createEntityRowMapper();

    public ProfileBaseDaoFacadeImpl(String entityStoredProcedureName) {
        super(entityStoredProcedureName);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(T obj) {
        MapSqlParameterSource map = createIdParameterMapper(
                obj.getId())
                .addValue("name", obj.getName())
                .addValue("qos_id", obj.getQosId())
                .addValue("description", obj.getDescription());
        return map;
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid guid) {
        return getCustomMapSqlParameterSource()
                .addValue("id", guid);
    }

    protected static abstract class ProfileBaseDaoFacadaeImplMapper<M extends ProfileBase> implements RowMapper<M> {
        @Override
        public M mapRow(ResultSet rs, int rowNum) throws SQLException {
            M entity = createProfileEntity(rs);
            entity.setId(getGuid(rs, "id"));
            entity.setName(rs.getString("name"));
            entity.setDescription(rs.getString("description"));
            entity.setQosId(getGuid(rs, "qos_id"));
            return entity;
        }

        /**
         * @param rs
         * @return entity of derived mapper class
         * @throws SQLException
         */
        protected abstract M createProfileEntity(ResultSet rs) throws SQLException;

    }
}
