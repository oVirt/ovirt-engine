package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code ActionGroupDaoImpl} provides a concrete implementation of {@link ActionGroupDao}.
 */
@Named
@Singleton
public class ActionGroupDaoImpl extends BaseDao implements ActionGroupDao {
    @Override
    public List<ActionGroup> getAllForRole(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        return getCallsHandler().executeReadList("GetRoleActionGroupsByRoleId",
                (rs, rowNum) -> ActionGroup.forValue(rs.getInt("action_group_id")),
                parameterSource);
    }
}
