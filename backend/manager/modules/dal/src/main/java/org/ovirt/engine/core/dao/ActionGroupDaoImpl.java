package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ActionVersionMap;
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

    private static final ConcurrentMap<VdcActionType, ActionVersionMap> cache = new ConcurrentHashMap<>();
    private static final ActionVersionMap nullActionVersionMap = new ActionVersionMap(true);

    @Override
    public List<ActionGroup> getAllForRole(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        return getCallsHandler().executeReadList("GetRoleActionGroupsByRoleId",
                ActionGroupMapper.instance,
                parameterSource);
    }

    @Override
    public ActionVersionMap getActionVersionMapByActionType(VdcActionType action_type) {
        ActionVersionMap result = cache.get(action_type);
        if (result != null) {
            if (result.isNullValue()) {
                return null;
            }
            return result;
        }
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("action_type", action_type);

        result = getCallsHandler().executeRead("Getaction_version_mapByaction_type",
                ActionVersionMapMapper.instance,
                parameterSource);
        if (result == null) {
            cache.putIfAbsent(action_type, nullActionVersionMap);
        } else {
            cache.putIfAbsent(action_type, result);
        }
        result = cache.get(action_type);
        if (result.isNullValue()) {
            return null;
        }
        return result;
    }

    @Override
    public void addActionVersionMap(ActionVersionMap actionVersionMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("action_type",
                actionVersionMap.getActionType()).addValue("cluster_minimal_version",
                actionVersionMap.getClusterMinimalVersion()).addValue("storage_pool_minimal_version",
                actionVersionMap.getStoragePoolMinimalVersion());

        getCallsHandler().executeModification("Insertaction_version_map", parameterSource);
    }

    @Override
    public void removeActionVersionMap(VdcActionType action_type) {
        cache.remove(action_type);
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("action_type", action_type);
        getCallsHandler().executeModification("Deleteaction_version_map", parameterSource);
    }

    @Override
    public List<ActionVersionMap> getAllActionVersionMap() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        return getCallsHandler().executeReadList("GetAllFromaction_version_map",
                ActionVersionMapMapper.instance,
                parameterSource);

    }

    private static class ActionVersionMapMapper implements RowMapper<ActionVersionMap> {
        public static final ActionVersionMapMapper instance = new ActionVersionMapMapper();

        @Override
        public ActionVersionMap mapRow(ResultSet rs, int rowNum) throws SQLException {
            ActionVersionMap entity = new ActionVersionMap();
            entity.setActionType(VdcActionType.forValue(rs.getInt("action_type")));
            entity.setClusterMinimalVersion(rs.getString("cluster_minimal_version"));
            entity.setStoragePoolMinimalVersion(rs.getString("storage_pool_minimal_version"));
            return entity;
        }
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
