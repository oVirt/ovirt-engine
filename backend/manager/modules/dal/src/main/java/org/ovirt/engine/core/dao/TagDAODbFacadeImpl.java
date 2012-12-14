package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.common.businessentities.TagsUserMap;
import org.ovirt.engine.core.common.businessentities.tags_vds_map;
import org.ovirt.engine.core.common.businessentities.tags_vm_map;
import org.ovirt.engine.core.common.businessentities.tags_vm_pool_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>TagDAODbFacadeImpl</code> provides an implementation of {@link TagDAO} that uses code refactored from the
 * {@link DbFacade} class.
 *
 *
 */
public class TagDAODbFacadeImpl extends BaseDAODbFacade implements TagDAO {
    private class TagRowMapper implements ParameterizedRowMapper<tags> {
        @Override
        public tags mapRow(ResultSet rs, int rowNum) throws SQLException {
            tags entity = new tags();
            entity.setdescription(getValueOrNull(rs, "description", ""));
            entity.settag_id(Guid.createGuidFromString(getValueOrNull(rs,
                    "tag_id", Guid.NewGuid().getUuid().toString())));
            entity.settag_name(getValueOrNull(rs, "tag_name", ""));
            entity.setparent_id(NGuid.createGuidFromString(getValueOrNull(rs,
                    "parent_id", Guid.NewGuid().getUuid().toString())));
            entity.setIsReadonly(rs.getBoolean("readonly"));
            entity.settype(TagsType.forValue(Integer.valueOf(getValueOrNull(rs,
                    "type", "0"))));
            return entity;
        }

        String getValueOrNull(ResultSet rs, String name, String defval) {
            String result = null;

            try {
                result = rs.getString(name);
            } catch (SQLException e) {
                // consume exception, fall back to default value
            }

            return result != null ? result : defval;
        }
    }

    @Override
    public tags get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_id", id);

        return getCallsHandler()
                        .executeRead("GettagsBytag_id", new TagRowMapper(), parameterSource);
    }

    @Override
    public tags getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_name", name);

        return getCallsHandler()
                .executeRead("GettagsBytag_name", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler()
                .executeReadList("GetAllFromtags", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAllForParent(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("parent_id", id);

        return getCallsHandler()
                .executeReadList("GettagsByparent_id", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAllForUserGroups(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("group_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByUserGroupId", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAllUserGroupTagsWithIds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_ids", ids);

        return getCallsHandler()
                .executeReadList("GetUserGroupTagsByTagIds", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAllForUsers(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByUserId", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAllForUsersWithIds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_ids", ids);

        return getCallsHandler()
                .executeReadList("GetUserTagsByTagIds", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAllForVds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVdsId", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAllForVdsWithIds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_ids", ids);

        return getCallsHandler()
                .executeReadList("GetVdsTagsByTagIds", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAllForVm(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVmId", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAllVmTagsWithIds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_ids", ids);

        return getCallsHandler()
                .executeReadList("GetVmTagsByTagId", new TagRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags> getAllForVmPools(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVmpoolId", new TagRowMapper(), parameterSource);
    }

    @Override
    public void save(tags tag) {
        Guid id = tag.gettag_id();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.NewGuid();
            tag.settag_id(id);
        }

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", tag.getdescription())
                .addValue("tag_id", tag.gettag_id())
                .addValue("tag_name", tag.gettag_name())
                .addValue("parent_id", tag.getparent_id())
                .addValue("readonly", tag.getIsReadonly())
                .addValue("type", tag.gettype());

        getCallsHandler().executeModification("Inserttags", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_id", id);

        getCallsHandler()
                .executeModification("Deletetags", parameterSource);
    }

    @Override
    public void update(tags tag) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", tag.getdescription())
                .addValue("tag_id", tag.gettag_id())
                .addValue("tag_name", tag.gettag_name())
                .addValue("parent_id", tag.getparent_id())
                .addValue("readonly", tag.getIsReadonly())
                .addValue("type", tag.gettype());

        getCallsHandler()
                .executeModification("Updatetags", parameterSource);
    }

    @Override
    public TagsUserGroupMap getTagUserGroupByGroupIdAndByTagId(Guid tag, Guid group) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("group_id", group)
                .addValue("tag_id", tag);

        ParameterizedRowMapper<TagsUserGroupMap> mapper = new ParameterizedRowMapper<TagsUserGroupMap>() {
            @Override
            public TagsUserGroupMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsUserGroupMap entity = new TagsUserGroupMap();
                entity.setgroup_id(Guid.createGuidFromString(rs.getString("group_id")));
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                return entity;
            }
        };

        return getCallsHandler()
                        .executeRead("GetTagUserGroupByGroupIdAndByTagId", mapper, parameterSource);
    }

    @Override
    public void attachUserGroupToTag(TagsUserGroupMap tagUserGroupMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("group_id",
                tagUserGroupMap.getgroup_id()).addValue("tag_id", tagUserGroupMap.gettag_id());

        getCallsHandler()
                .executeModification("Inserttags_user_group_map", parameterSource);
    }

    @Override
    public void detachUserGroupFromTag(Guid tagId, Guid groupId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("group_id", groupId)
                .addValue("tag_id", tagId);

        getCallsHandler()
                .executeModification("Deletetags_user_group_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TagsUserGroupMap> getTagUserGroupMapsForTagName(String tagName) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_name", tagName);

        ParameterizedRowMapper<TagsUserGroupMap> mapper = new ParameterizedRowMapper<TagsUserGroupMap>() {
            @Override
            public TagsUserGroupMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsUserGroupMap entity = new TagsUserGroupMap();
                entity.setgroup_id(Guid.createGuidFromString(rs.getString("group_id")));
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                return entity;
            }
        };

        return getCallsHandler()
                        .executeReadList("Gettags_user_group_mapByTagName", mapper, parameterSource);
    }

    @Override
    public TagsUserMap getTagUserByTagIdAndByuserId(Guid tagId, Guid userId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "user_id", userId);

        ParameterizedRowMapper<TagsUserMap> mapper = new ParameterizedRowMapper<TagsUserMap>() {
            @Override
            public TagsUserMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsUserMap entity = new TagsUserMap();
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                entity.setuser_id(Guid.createGuidFromString(rs.getString("user_id")));
                return entity;
            }
        };

        return getCallsHandler()
                        .executeRead("GetTagUserByTagIdAndByuserId", mapper, parameterSource);
    }

    @Override
    public void attachUserToTag(TagsUserMap tagUserMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagUserMap.gettag_id()).addValue("user_id", tagUserMap.getuser_id());

        getCallsHandler()
                .executeModification("Inserttags_user_map", parameterSource);
    }

    @Override
    public void detachUserFromTag(Guid tagId, Guid userId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "user_id", userId);

        getCallsHandler()
                .executeModification("Deletetags_user_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TagsUserMap> getTagUserMapByTagName(String tagName) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_name", tagName);

        ParameterizedRowMapper<TagsUserMap> mapper = new ParameterizedRowMapper<TagsUserMap>() {
            @Override
            public TagsUserMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsUserMap entity = new TagsUserMap();
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                entity.setuser_id(Guid.createGuidFromString(rs.getString("user_id")));
                return entity;
            }
        };

        return getCallsHandler()
                .executeReadList("Gettags_user_mapByTagName", mapper, parameterSource);
    }

    @Override
    public tags_vds_map getTagVdsByTagIdAndByVdsId(Guid tagId, Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vds_id", vdsId);

        ParameterizedRowMapper<tags_vds_map> mapper = new ParameterizedRowMapper<tags_vds_map>() {
            @Override
            public tags_vds_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                tags_vds_map entity = new tags_vds_map();
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                entity.setvds_id(Guid.createGuidFromString(rs.getString("vds_id")));
                return entity;
            }
        };

        return getCallsHandler()
                        .executeRead("GetTagVdsBytagIdAndByVdsId", mapper, parameterSource);
    }

    @Override
    public void attachVdsToTag(tags_vds_map tagVdsMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagVdsMap.gettag_id()).addValue("vds_id", tagVdsMap.getvds_id());

        getCallsHandler()
                .executeModification("Inserttags_vds_map", parameterSource);
    }

    @Override
    public void detachVdsFromTag(Guid tagId, Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vds_id", vdsId);

        getCallsHandler()
                .executeModification("Deletetags_vds_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags_vds_map> getTagVdsMapByTagName(String tagName) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_name", tagName);

        ParameterizedRowMapper<tags_vds_map> mapper = new ParameterizedRowMapper<tags_vds_map>() {
            @Override
            public tags_vds_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                tags_vds_map entity = new tags_vds_map();
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                entity.setvds_id(Guid.createGuidFromString(rs.getString("vds_id")));
                return entity;
            }
        };

        return getCallsHandler()
                .executeReadList("Gettags_vds_mapByTagName", mapper, parameterSource);
    }

    @Override
    public tags_vm_map getTagVmByTagIdAndByVmId(Guid tagId, Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vm_id", vmId);

        ParameterizedRowMapper<tags_vm_map> mapper = new ParameterizedRowMapper<tags_vm_map>() {
            @Override
            public tags_vm_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                tags_vm_map entity = new tags_vm_map();
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                entity.setvm_id(Guid.createGuidFromString(rs.getString("vm_id")));
                entity.setDefaultDisplayType((Integer) rs.getObject("DefaultDisplayType"));
                return entity;
            }
        };

        return getCallsHandler()
                .executeRead("GetTagVmByTagIdAndByvmId", mapper, parameterSource);
    }

    @Override
    public void attachVmToTag(tags_vm_map tagVmMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagVmMap.gettag_id()).addValue("vm_id", tagVmMap.getvm_id()).addValue("DefaultDisplayType",
                tagVmMap.getDefaultDisplayType());

        getCallsHandler()
                .executeModification("Inserttags_vm_map", parameterSource);
    }

    @Override
    public void updateDefaultDisplayForVmTag(tags_vm_map tagsVmMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagsVmMap.gettag_id()).addValue("vm_id", tagsVmMap.getvm_id()).addValue("DefaultDisplayType",
                tagsVmMap.getDefaultDisplayType());

        getCallsHandler()
                .executeModification("UpdateVmTagsDefaultDisplayType", parameterSource);
    }

    @Override
    public void detachVmFromTag(Guid tagId, Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vm_id", vmId);

        getCallsHandler()
                .executeModification("Deletetags_vm_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags_vm_map> getTagVmMapByTagName(String tagName) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_name", tagName);

        ParameterizedRowMapper<tags_vm_map> mapper = new ParameterizedRowMapper<tags_vm_map>() {
            @Override
            public tags_vm_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                tags_vm_map entity = new tags_vm_map();
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                entity.setvm_id(Guid.createGuidFromString(rs.getString("vm_id")));
                return entity;
            }
        };

        return getCallsHandler()
                .executeReadList("Gettags_vm_mapByTagName", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags_vm_map> getTagVmMapByVmIdAndDefaultTag(Guid vmid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", vmid);

        ParameterizedRowMapper<tags_vm_map> mapper = new ParameterizedRowMapper<tags_vm_map>() {
            @Override
            public tags_vm_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                tags_vm_map entity = new tags_vm_map();
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                entity.setvm_id(Guid.createGuidFromString(rs.getString("vm_id")));
                entity.setDefaultDisplayType((Integer) rs.getObject("DefaultDisplayType"));
                return entity;
            }
        };

        return getCallsHandler()
                        .executeReadList(
                                "GetnVmTagsByVmIdAndDefaultTag", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags_vm_map> getTimeLeasedUserVmsByAdGroupAndVmPoolId(Guid adGroupId, Guid vmPoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("groupId", adGroupId)
                .addValue("vm_pool_id", vmPoolId);

        ParameterizedRowMapper<tags_vm_map> mapper = new ParameterizedRowMapper<tags_vm_map>() {
            @Override
            public tags_vm_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                tags_vm_map entity = new tags_vm_map();
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                entity.setvm_id(Guid.createGuidFromString(rs.getString("vm_id")));
                entity.setDefaultDisplayType((Integer) rs.getObject("DefaultDisplayType"));
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                return entity;
            }
        };

        return getCallsHandler()
                        .executeReadList(
                                "GetTimeLeasedUsersVmsByGroupIdAndPoolId", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<tags_vm_pool_map> getVmPoolTagsByVmPoolIdAndAdElementId(NGuid vmPoolId, Guid adElementId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("ad_id", adElementId)
                .addValue("vm_pool_id", vmPoolId);

        ParameterizedRowMapper<tags_vm_pool_map> mapper = new ParameterizedRowMapper<tags_vm_pool_map>() {
            @Override
            public tags_vm_pool_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                tags_vm_pool_map entity = new tags_vm_pool_map();
                entity.settag_id(Guid.createGuidFromString(rs.getString("tag_id")));
                entity.setvm_pool_id(Guid.createGuidFromString(rs.getString("vm_pool_id")));
                return entity;
            }
        };

        return getCallsHandler()
                        .executeReadList(
                                "GetVmPoolTagsByVmPoolIdAndAdElementId", mapper, parameterSource);
    }
}
