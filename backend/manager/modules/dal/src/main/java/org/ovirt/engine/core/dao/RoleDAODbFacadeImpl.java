package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.roles;
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
    public roles get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        return getCallsHandler().executeRead("GetRolsByid", new RolesRowMapper(), parameterSource);
    }

    @Override
    public roles getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("name", name);

        return getCallsHandler().executeRead("GetRoleByName", new RolesRowMapper(), parameterSource);
    }

    @Override
    public List<roles> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromRole", new RolesRowMapper(), parameterSource);
    }

    @Override
    public List<roles> getAllForAdElement(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("ad_element_id", id);

        return getCallsHandler().executeReadList("GetAllRolesByAdElementId", new RolesRowMapper(), parameterSource);
    }

    @Override
    public List<roles> getForAdElement(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("ad_element_id", id);

        return getCallsHandler().executeReadList("GetRolesByAdElementId", new RolesRowMapper(), parameterSource);
    }

    @Override
    public void save(roles role) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", role.getdescription())
                .addValue("id", role.getId()).addValue("name", role.getname())
                .addValue("is_readonly", role.getis_readonly())
                .addValue("role_type", role.getType().getId());

        getCallsHandler().executeModification("InsertRole", parameterSource);
    }

    @Override
    public void update(roles role) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", role.getdescription())
                .addValue("id", role.getId()).addValue("name", role.getname())
                .addValue("is_readonly", role.getis_readonly())
                .addValue("role_type", role.getType().getId());

        getCallsHandler().executeModification("UpdateRole", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("DeleteRole", parameterSource);
    }
}
