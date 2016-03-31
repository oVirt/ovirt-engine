package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>ActionGroupDaoImpl</code> provides a concrete implementation of {@link ActionGroupDao}.
 *
 * The initial implementation came from  {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 */
@Named
@Singleton
public class ActionGroupDaoImpl extends BaseDao implements ActionGroupDao {
    @Override
    public List<ActionGroup> getAllForRole(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        return getCallsHandler().executeReadList("GetRoleActionGroupsByRoleId",
                ActionGroupMapper.instance,
                parameterSource);
    }

    private static class ActionGroupMapper implements RowMapper<ActionGroup> {
        public static final ActionGroupMapper instance = new ActionGroupMapper();

        @Override
        public ActionGroup mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            return ActionGroup.forValue(rs.getInt("action_group_id"));
        }
    }
}
