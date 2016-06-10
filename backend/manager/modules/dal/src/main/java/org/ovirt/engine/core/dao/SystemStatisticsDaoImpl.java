package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class SystemStatisticsDaoImpl implements SystemStatisticsDao {

    private final JdbcTemplate jdbcTemplate;
    private final DbEngineDialect dbEngineDialect;

    @Inject
    SystemStatisticsDaoImpl(JdbcTemplate jdbcTemplate, DbEngineDialect dbEngineDialect) {
        Objects.requireNonNull(jdbcTemplate);
        Objects.requireNonNull(dbEngineDialect);

        this.jdbcTemplate = jdbcTemplate;
        this.dbEngineDialect = dbEngineDialect;
    }

    @Override
    public Integer getSystemStatisticsValue(String entity) {
        return getSystemStatisticsValue(entity, "");
    }

    @Override
    public Integer getSystemStatisticsValue(String entity, String status) {
        MapSqlParameterSource parameterSource =
                new CustomMapSqlParameterSource(dbEngineDialect)
                        .addValue("entity", entity)
                        .addValue("status", status);

        RowMapper<Integer> mapper = (rs, rowNum) -> rs.getInt("val");

        Map<String, Object> dbResults =
                dbEngineDialect
                        .createJdbcCallForQuery(jdbcTemplate)
                        .withProcedureName("Getsystem_statistics")
                        .returningResultSet("RETURN_VALUE", mapper)
                        .execute(parameterSource);

        return (Integer) DbFacadeUtils.asSingleResult((List<?>) dbResults.get("RETURN_VALUE"));
    }
}
