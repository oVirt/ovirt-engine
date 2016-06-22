package org.ovirt.engine.core.dao;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

@Named
@Singleton
class EntityDaoImpl implements EntityDao {
    private final JdbcTemplate jdbcTemplate;
    private final DbEngineDialect dbEngineDialect;
    private final Provider<CustomMapSqlParameterSource> sqlParameterSourceProvider;

    @Inject
    EntityDaoImpl(
            JdbcTemplate jdbcTemplate,
            DbEngineDialect dbEngineDialect,
            Provider<CustomMapSqlParameterSource> sqlParameterSourceProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbEngineDialect = dbEngineDialect;
        this.sqlParameterSourceProvider = sqlParameterSourceProvider;
    }

    @Override
    public String getEntityNameByIdAndType(Guid objectId, VdcObjectType vdcObjectType) {
        MapSqlParameterSource parameterSource =
                sqlParameterSourceProvider.get()
                        .addValue("entity_id", objectId)
                        .addValue("object_type", vdcObjectType.getValue());

        Map<String, Object> dbResults =
                new SimpleJdbcCall(jdbcTemplate)
                        .withFunctionName("fn_get_entity_name")
                        .execute(parameterSource);

        String resultKey = dbEngineDialect.getFunctionReturnKey();
        return dbResults.get(resultKey) != null ? dbResults.get(resultKey).toString() : null;
    }

}
