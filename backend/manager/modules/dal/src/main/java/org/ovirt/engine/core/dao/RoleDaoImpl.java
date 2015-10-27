package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>RoleDaoImpl</code> provides a concrete implementation of {@link RoleDao} using code refactored from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 *
 */
@Named
@Singleton
public class RoleDaoImpl extends BaseDao implements RoleDao {

    private static class RolesRowMapper implements RowMapper<Role> {

        public static final RolesRowMapper instance = new RolesRowMapper();

        @Override
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
            Role entity = new Role();
            entity.setDescription(rs.getString("description"));
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setName(rs.getString("name"));
            entity.setReadonly(rs.getBoolean("is_readonly"));
            entity.setType(RoleType.getById(rs.getInt("role_type")));
            entity.setAllowsViewingChildren(rs.getBoolean("allows_viewing_children"));
            entity.setAppMode(ApplicationMode.from(rs.getInt("app_mode")));
            return entity;
        }
    }

    @Override
    public Role get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        return getCallsHandler().executeRead("GetRolsByid", RolesRowMapper.instance, parameterSource);
    }

    @Override
    public Role getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("name", name);

        return getCallsHandler().executeRead("GetRoleByName", RolesRowMapper.instance, parameterSource);
    }

    @Override
    public List<Role> getAll() {
        Integer appMode = Config.<Integer> getValue(ConfigValues.ApplicationMode);
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("app_mode", appMode);

        return getCallsHandler().executeReadList("GetAllFromRole", RolesRowMapper.instance, parameterSource);
    }

    @Override
    public List<Role> getAllNonAdminRoles() {
        Integer appMode = Config.<Integer> getValue(ConfigValues.ApplicationMode);
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("app_mode", appMode);

        return getCallsHandler().executeReadList("GetAllNonAdminRoles", RolesRowMapper.instance, parameterSource);
    }

    @Override
    public List<Role> getAnyAdminRoleForUserAndGroups(Guid id, String groupIds) {
        Integer appMode = Config.<Integer> getValue(ConfigValues.ApplicationMode);
        return getAnyAdminRoleForUserAndGroups(id, groupIds, appMode.intValue());
    }

    @Override
    public List<Role> getAnyAdminRoleForUserAndGroups(Guid id, String groupIds, int appMode) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id)
                .addValue("group_ids", groupIds)
                .addValue("app_mode", appMode);
        return getCallsHandler().executeReadList("GetAnyAdminRoleByUserIdAndGroupIds",
                RolesRowMapper.instance,
                parameterSource);
    }

    @Override
    public void save(Role role) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", role.getDescription())
                .addValue("id", role.getId()).addValue("name", role.getName())
                .addValue("is_readonly", role.isReadonly())
                .addValue("role_type", role.getType().getId())
                .addValue("allows_viewing_children", role.allowsViewingChildren())
                .addValue("app_mode", role.getAppMode().getValue());

        getCallsHandler().executeModification("InsertRole", parameterSource);
    }

    @Override
    public void update(Role role) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", role.getDescription())
                .addValue("id", role.getId()).addValue("name", role.getName())
                .addValue("is_readonly", role.isReadonly())
                .addValue("role_type", role.getType().getId())
                .addValue("allows_viewing_children", role.allowsViewingChildren());

        getCallsHandler().executeModification("UpdateRole", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("DeleteRole", parameterSource);
    }
}
