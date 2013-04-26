package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.RolesRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>RoleDAODbFacadeImpl</code> provides a concrete implementation of {@link RoleDAO} using code refactored from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 *
 */
public class RoleDAODbFacadeImpl extends BaseDAODbFacade implements RoleDAO {

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
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromRole", RolesRowMapper.instance, parameterSource);
    }

    @Override
    public List<Role> getAllForUserAndGroups(Guid id, String groupIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id)
                .addValue("group_ids", groupIds);
        return getCallsHandler().executeReadList("GetAllRolesByUserIdAndGroupIds",
                RolesRowMapper.instance,
                parameterSource);

    }

    @Override
    public void save(Role role) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", role.getdescription())
                .addValue("id", role.getId()).addValue("name", role.getname())
                .addValue("is_readonly", role.getis_readonly())
                .addValue("role_type", role.getType().getId())
                .addValue("allows_viewing_children", role.allowsViewingChildren());

        getCallsHandler().executeModification("InsertRole", parameterSource);
    }

    @Override
    public void update(Role role) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", role.getdescription())
                .addValue("id", role.getId()).addValue("name", role.getname())
                .addValue("is_readonly", role.getis_readonly())
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
