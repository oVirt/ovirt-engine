package org.ovirt.engine.core.dao.dwh;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeeping;
import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeepingVariable;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.dao.BaseDao;
import org.springframework.jdbc.core.RowMapper;

@Named
@Singleton
public class DwhHistoryTimekeepingDaoImpl extends BaseDao implements DwhHistoryTimekeepingDao {
    private static class DwhHistoryTimekeepingMapper implements RowMapper<DwhHistoryTimekeeping> {
        private static final DwhHistoryTimekeepingMapper instance = new DwhHistoryTimekeepingMapper();

        @Override
        public DwhHistoryTimekeeping mapRow(ResultSet rs, int rowNum) throws SQLException {
            DwhHistoryTimekeeping entity = new DwhHistoryTimekeeping();
            entity.setVariable(DwhHistoryTimekeepingVariable.forVarName(rs.getString("var_name")));
            entity.setValue(rs.getString("var_value"));
            entity.setDateTime(DbFacadeUtils.fromDate(rs.getTimestamp("var_datetime")));
            return entity;
        }
    }

    @Override
    public DwhHistoryTimekeeping get(DwhHistoryTimekeepingVariable variable) {
        return getCallsHandler().executeRead("GetDwhHistoryTimekeepingByVarName",
                DwhHistoryTimekeepingMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("var_name", variable.getVarName()));
    }

    @Override
    public void save(DwhHistoryTimekeeping variable) {
        getCallsHandler().executeModification("UpdateDwhHistoryTimekeeping", getCustomMapSqlParameterSource()
                .addValue("var_name", variable.getVariable().getVarName())
                .addValue("var_value", variable.getValue())
                .addValue("var_datetime", variable.getDateTime()));
    }
}
