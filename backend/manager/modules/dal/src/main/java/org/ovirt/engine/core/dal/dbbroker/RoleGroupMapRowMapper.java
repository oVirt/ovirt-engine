package org.ovirt.engine.core.dal.dbbroker;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.compat.Guid;

public class RoleGroupMapRowMapper implements ParameterizedRowMapper<RoleGroupMap> {
    @Override
    public RoleGroupMap mapRow(ResultSet rs, int rowNum) throws SQLException {
        RoleGroupMap entity = new RoleGroupMap(ActionGroup.forValue(rs.getInt("action_group_id")),
                Guid.createGuidFromString(rs.getString("role_id")));
        return entity;
    }
}
