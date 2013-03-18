package org.ovirt.engine.core.dal.dbbroker;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;

public class RolesRowMapper implements RowMapper<Role> {

    public static final RolesRowMapper instance = new RolesRowMapper();

    @Override
    public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
        Role entity = new Role();
        entity.setdescription(rs.getString("description"));
        entity.setId(Guid.createGuidFromString(rs.getString("id")));
        entity.setname(rs.getString("name"));
        entity.setis_readonly(rs.getBoolean("is_readonly"));
        entity.setType(RoleType.getById(rs.getInt("role_type")));
        entity.setAllowsViewingChildren(rs.getBoolean("allows_viewing_children"));
        return entity;
    }

}
