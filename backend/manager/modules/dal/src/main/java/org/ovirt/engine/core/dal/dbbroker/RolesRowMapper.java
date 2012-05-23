package org.ovirt.engine.core.dal.dbbroker;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class RolesRowMapper implements ParameterizedRowMapper<roles> {

    public static final RolesRowMapper instance = new RolesRowMapper();

    @Override
    public roles mapRow(ResultSet rs, int rowNum) throws SQLException {
        roles entity = new roles();
        entity.setdescription(rs.getString("description"));
        entity.setId(Guid.createGuidFromString(rs.getString("id")));
        entity.setname(rs.getString("name"));
        entity.setis_readonly(rs.getBoolean("is_readonly"));
        entity.setType(RoleType.getById(rs.getInt("role_type")));
        entity.setAllowsViewingChildren(rs.getBoolean("allows_viewing_children"));
        return entity;
    }

}
