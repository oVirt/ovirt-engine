package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code PermissionsDaoImpl} provides a concrete implementation of {@link PermissionDao}.
 */
@Named
@Singleton
public class PermissionDaoImpl extends BaseDao implements PermissionDao {
    @Override
    public Permission get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        return getCallsHandler().executeRead("GetPermissionsByid", permissionRowMapper, parameterSource);
    }

    @Override
    public List<Permission> getConsumedPermissionsForQuotaId(Guid quotaId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("quota_id", quotaId);

        return getCallsHandler().executeReadList("GetConsumedPermissionsForQuotaId",
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public Permission getForRoleAndAdElementAndObject(Guid roleid,
            Guid elementid, Guid objectid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("role_id", roleid)
                .addValue("ad_element_id", elementid)
                .addValue("object_id", objectid);

        return getCallsHandler().executeRead("GetPermissionsByRoleIdAndAdElementIdAndObjectId",
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public Permission getForRoleAndAdElementAndObjectWithGroupCheck(Guid roleid,
            Guid elementid, Guid objectid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("role_id", roleid)
                .addValue("ad_element_id", elementid)
                .addValue("object_id", objectid);

        return getCallsHandler().executeRead("GetForRoleAndAdElementAndObject_wGroupCheck",
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public List<Permission> getAllForAdElement(Guid id) {
        return getAllForAdElement(id, -1, false);
    }

    @Override
    public List<Permission> getAllForAdElement(Guid id, long engineSessionSeqId, boolean isFiltered) {
        int appMode = Config.<Integer> getValue(ConfigValues.ApplicationMode);

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("ad_element_id", id).
                addValue("engine_session_seq_id", engineSessionSeqId).
                addValue("is_filtered", isFiltered).
                addValue("app_mode", appMode);

        return getCallsHandler().executeReadList("GetPermissionsByAdElementId",
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public List<Permission> getAllForAdElementAndGroups(Guid id, Guid currentUserId, Collection<Guid> groupIds, boolean isFiltered) {
        int appMode = Config.<Integer> getValue(ConfigValues.ApplicationMode);

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
            .addValue("ad_element_id", id)
            .addValue("user_id", currentUserId)
            .addValue("user_groups", createArrayOf("uuid", groupIds.toArray()))
            .addValue("is_filtered", isFiltered)
            .addValue("app_mode", appMode);

        return getCallsHandler().executeReadList("GetPermissionsByAdElementIdAndGroupIds",
            permissionRowMapper,
            parameterSource);
    }

    @Override
    public List<Permission> getAllDirectPermissionsForAdElement(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("ad_element_id", id);

        return getCallsHandler().executeReadList("GetDirectPermissionsByAdElementId",
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public List<Permission> getAllForRole(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("role_id", id);

        return getCallsHandler().executeReadList("GetPermissionsByRoleId",
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public List<Permission> getAllForRoleAndAdElement(Guid roleid,
            Guid elementid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("role_id", roleid).addValue("ad_element_id",
                        elementid);

        return getCallsHandler().executeReadList("GetPermissionsByRoleIdAndAdElementId",
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public List<Permission> getAllForRoleAndObject(Guid roleid, Guid objectid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("role_id", roleid).addValue("object_id", objectid);

        return getCallsHandler().executeReadList("GetPermissionsByRoleIdAndObjectId",
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public List<Permission> getAllForAdElementAndObjectId(Guid elementid, Guid objectid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("ad_element_id", elementid).addValue("object_id", objectid);

        return getCallsHandler().executeReadList("GetPermissionsByAdElementAndObjectId",
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public List<Permission> getAllForEntity(Guid id) {
        return getAllForEntity(id, -1, false);
    }

    @Override
    public List<Permission> getAllForEntity(Guid id, long engineSessionId, boolean isFiltered) {
        return getAllForEntity(id, engineSessionId, isFiltered, false);
    }

    @Override
    public List<Permission> getAllForEntity(Guid id, long engineSessionId, boolean isFiltered, boolean allUsersWithPermission) {
        int appMode = Config.<Integer> getValue(ConfigValues.ApplicationMode);
        return getAllForEntity(id, engineSessionId, isFiltered, allUsersWithPermission, appMode);
    }

    @Override
    public List<Permission> getAllForEntity(Guid id, long engineSessionSeqId, boolean isFiltered, boolean allUsersWithPermission, int appMode) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id)
                .addValue("engine_session_seq_id", engineSessionSeqId)
                .addValue("is_filtered", isFiltered)
                .addValue("app_mode", appMode);
        String functionName = "GetPermissionsByEntityId";
        if (allUsersWithPermission) {
            functionName = "GetAllUsersWithPermissionsOnEntityByEntityId";
        }
        return getCallsHandler().executeReadList(functionName,
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public List<Permission> getTreeForEntity(Guid id, VdcObjectType type) {
        return getTreeForEntity(id, type, -1, false);
    }

    @Override
    public List<Permission> getTreeForEntity(Guid id, VdcObjectType type, long engineSessionSeqId, boolean isFiltered) {
        int appMode = Config.<Integer> getValue(ConfigValues.ApplicationMode);
        return getTreeForEntity(id, type, engineSessionSeqId, isFiltered, appMode);
    }

    @Override
    public List<Permission> getTreeForEntity(Guid id, VdcObjectType type, long engineSessionSeqId, boolean isFiltered, int appMode) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("object_type_id", type.getValue())
                        .addValue("engine_session_seq_id", engineSessionSeqId)
                        .addValue("is_filtered", isFiltered)
                        .addValue("app_mode", appMode);
        return getCallsHandler().executeReadList("GetPermissionsTreeByEntityId",
                permissionRowMapper,
                parameterSource);
    }

    @Override
    public Guid getEntityPermissions(Guid adElementId, ActionGroup actionGroup, Guid objectId,
                                     VdcObjectType vdcObjectType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("user_id", adElementId)
                .addValue("action_group_id", actionGroup.getId()).addValue("object_id", objectId).addValue(
                        "object_type_id", vdcObjectType.getValue());

        return getCallsHandler().executeRead("get_entity_permissions",
                createGuidMapper(),
                parameterSource);
    }

    @Override
    public Guid getEntityPermissionsForUserAndGroups(Guid userId,
                                                     String groupIds,
                                                     ActionGroup actionGroup,
                                                     Guid objectId,
                                                     VdcObjectType vdcObjectType,
                                                     boolean ignoreEveryone) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("user_id", userId)
                        .addValue("group_ids", groupIds)
                        .addValue("action_group_id", actionGroup.getId())
                        .addValue("object_id", objectId)
                        .addValue("object_type_id", vdcObjectType.getValue())
                        .addValue("ignore_everyone", ignoreEveryone);

        return getCallsHandler().executeRead("get_entity_permissions_for_user_and_groups",
                createGuidMapper(),
                parameterSource);
    }

    @Override
    public void save(Permission permission) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("ad_element_id", permission.getAdElementId())
                .addValue("id", permission.getId())
                .addValue("role_id", permission.getRoleId())
                .addValue("object_id", permission.getObjectId())
                .addValue("object_type_id",
                        permission.getObjectType().getValue());

        getCallsHandler().executeModification("InsertPermission", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("DeletePermission", parameterSource);
    }

    @Override
    public void removeForEntity(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);
        getCallsHandler().executeModification("DeletePermissionsByEntityId", parameterSource);
    }

    @Override
    public List<Permission> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(Permission entity) {
        throw new UnsupportedOperationException();
    }

    private static final RowMapper<Permission> permissionRowMapper = (rs, rowNum) -> {
        Permission entity = new Permission();
        entity.setAdElementId(getGuidDefaultEmpty(rs, "ad_element_id"));
        entity.setId(getGuidDefaultEmpty(rs, "id"));
        entity.setRoleId(getGuidDefaultEmpty(rs, "role_id"));
        entity.setObjectId(getGuidDefaultEmpty(rs, "object_id"));
        entity.setObjectType(VdcObjectType.forValue(rs
                .getInt("object_type_id")));
        entity.setRoleName(rs.getString("role_name"));
        entity.setObjectName(rs.getString("object_name"));
        entity.setOwnerName(rs.getString("owner_name"));
        entity.setNamespace(rs.getString("namespace"));
        entity.setAuthz(rs.getString("authz"));
        entity.setRoleType(RoleType.getById(rs.getInt("role_type")));
        entity.setCreationDate(rs.getLong("creation_date"));

        return entity;
    };
}
