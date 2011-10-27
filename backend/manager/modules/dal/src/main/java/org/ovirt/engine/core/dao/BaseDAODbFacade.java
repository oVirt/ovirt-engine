package org.ovirt.engine.core.dao;

import org.springframework.jdbc.core.JdbcTemplate;

import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.SimpleJdbcCallsHandler;

public abstract class BaseDAODbFacade {

    protected JdbcTemplate jdbcTemplate;
    protected DbEngineDialect dialect;
    protected DbFacade dbFacade;

    public void setTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setDialect(DbEngineDialect dialect) {
        this.dialect = dialect;
    }

    public static final String RETURN_VALUE_PARAMETER = "RETURN_VALUE";

    public BaseDAODbFacade() {
    }

    protected CustomMapSqlParameterSource getCustomMapSqlParameterSource() {
        return new CustomMapSqlParameterSource(dialect);
    }

    protected SimpleJdbcCallsHandler getCallsHandler() {
        return dbFacade.getCallsHandler();
    }

    public void setDbFacade(DbFacade dbFacade) {
        this.dbFacade = dbFacade;
    }
}
