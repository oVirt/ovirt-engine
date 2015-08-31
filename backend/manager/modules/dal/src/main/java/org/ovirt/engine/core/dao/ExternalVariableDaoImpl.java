package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.ExternalVariable;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;

@Named
@Singleton
public class ExternalVariableDaoImpl extends BaseDao implements ExternalVariableDao {

    private static class ExternalVariableMapper implements RowMapper<ExternalVariable> {
        private static final ExternalVariableMapper instance = new ExternalVariableMapper();

        @Override
        public ExternalVariable mapRow(ResultSet rs, int rowNum) throws SQLException {
            ExternalVariable entity = new ExternalVariable();
            entity.setName(rs.getString("var_name"));
            entity.setValue(rs.getString("var_value"));
            entity.setUpdateDate(DbFacadeUtils.fromDate(rs.getTimestamp("_update_date")));
            return entity;
        }
    }

    @Override
    public void save(ExternalVariable var) {
        getCallsHandler().executeModification(
                "InsertExternalVariable",
                getCustomMapSqlParameterSource()
                        .addValue("var_name", var.getName())
                        .addValue("var_value", var.getValue())
        );
    }

    @Override
    public void update(ExternalVariable var) {
        getCallsHandler().executeModification(
                "UpdateExternalVariable",
                getCustomMapSqlParameterSource()
                        .addValue("var_name", var.getName())
                        .addValue("var_value", var.getValue())
        );
    }

    @Override
    public void saveOrUpdate(ExternalVariable var) {
        getCallsHandler().executeModification(
                "UpsertExternalVariable",
                getCustomMapSqlParameterSource()
                        .addValue("var_name", var.getName())
                        .addValue("var_value", var.getValue())
        );
    }

    @Override
    public void remove(String name) {
        getCallsHandler().executeModification(
                "DeleteExternalVariable",
                getCustomMapSqlParameterSource()
                        .addValue("var_name", name)
        );
    }

    @Override
    public ExternalVariable get(String name) {
        return getCallsHandler().executeRead(
                "GetExternalVariableByName",
                ExternalVariableMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("var_name", name)
        );
    }
}
