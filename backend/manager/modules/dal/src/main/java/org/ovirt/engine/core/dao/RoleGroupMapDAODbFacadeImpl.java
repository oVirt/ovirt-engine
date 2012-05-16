package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.RoleGroupMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>RoleGroupMapDAODbFacadeImpl</code> provides a concrete implementation of {@link RoleGroupMapDAO} using
 * functionality refactored from {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 */
public class RoleGroupMapDAODbFacadeImpl extends BaseDAODbFacade implements RoleGroupMapDAO {

    @Override
    public RoleGroupMap getByActionGroupAndRole(ActionGroup group, Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("action_group_id", group.getId()).addValue("role_id", id);

        return getCallsHandler().executeRead("Get_roles_groups_By_action_group_id_And_By_role_id",
                RoleGroupMapRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<RoleGroupMap> getAllForRole(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("role_id", id);

        return getCallsHandler().executeReadList("Get_role_groups_By_role_id",
                RoleGroupMapRowMapper.instance,
                parameterSource);
    }

    @Override
    public void save(RoleGroupMap map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("action_group_id", map.getActionGroup().getId())
                .addValue("role_id", map.getRoleId());

        getCallsHandler().executeModification("Insert_roles_groups", parameterSource);
    }

    @Override
    public void remove(ActionGroup group, Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("action_group_id", group.getId()).addValue("role_id",
                        id);

        getCallsHandler().executeModification("Delete_roles_groups", parameterSource);
    }
}
