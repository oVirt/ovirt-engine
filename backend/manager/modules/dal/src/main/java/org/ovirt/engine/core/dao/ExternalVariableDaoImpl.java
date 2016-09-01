package org.ovirt.engine.core.dao;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.ExternalVariable;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;

@Named
@Singleton
public class ExternalVariableDaoImpl extends BaseDao implements ExternalVariableDao {

    private static final RowMapper<ExternalVariable> externalVariableMapper = (rs, rowNum) -> {
        ExternalVariable entity = new ExternalVariable();
        entity.setName(rs.getString("var_name"));
        entity.setValue(rs.getString("var_value"));
        entity.setUpdateDate(DbFacadeUtils.fromDate(rs.getTimestamp("_update_date")));
        return entity;
    };

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
                externalVariableMapper,
                getCustomMapSqlParameterSource()
                        .addValue("var_name", name)
        );
    }
}
