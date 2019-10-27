package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.TagsTemplateMap;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.common.businessentities.TagsUserMap;
import org.ovirt.engine.core.common.businessentities.TagsVdsMap;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code TagDaoImpl} provides an implementation of {@link TagDao}.
 */
@Named
@Singleton
public class TagDaoImpl extends BaseDao implements TagDao {
    private static final RowMapper<Tags> tagRowMapper = (rs, rowNum) -> {
        Tags entity = new Tags();
        entity.setDescription(getValueOrNull(rs, "description", ""));
        entity.setTagId(getGuidDefaultNewGuid(rs, "tag_id"));
        entity.setTagName(getValueOrNull(rs, "tag_name", ""));
        entity.setParentId(getGuidDefaultNewGuid(rs, "parent_id"));
        entity.setIsReadonly(rs.getBoolean("readonly"));
        entity.setType(TagsType.forValue(Integer.parseInt(getValueOrNull(rs, "type", "0"))));
        return entity;
    };

    private static String getValueOrNull(ResultSet rs, String name, String defval) {
        String result = null;

        try {
            result = rs.getString(name);
        } catch (SQLException e) {
            // consume exception, fall back to default value
        }

        return result != null ? result : defval;
    }

    @Override
    public Tags get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_id", id);

        return getCallsHandler()
                .executeRead("GettagsBytag_id", tagRowMapper, parameterSource);
    }

    @Override
    public Tags getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_name", name);

        return getCallsHandler()
                .executeRead("GettagsBytag_name", tagRowMapper, parameterSource);
    }

    @Override
    public List<Tags> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler()
                .executeReadList("GetAllFromtags", tagRowMapper, parameterSource);
    }

    @Override
    public List<Tags> getAllForParent(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("parent_id", id);

        return getCallsHandler()
                .executeReadList("GettagsByparent_id", tagRowMapper, parameterSource);
    }

    @Override
    public List<Tags> getAllForUserGroups(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("group_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByUserGroupId", tagRowMapper, parameterSource);
    }

    @Override
    public List<Tags> getAllForUsers(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByUserId", tagRowMapper, parameterSource);
    }

    @Override
    public List<Tags> getAllForUsersWithIds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_ids", ids);

        return getCallsHandler()
                .executeReadList("GetUserTagsByTagIds", tagRowMapper, parameterSource);
    }

    @Override
    public List<Tags> getAllForVds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVdsId", tagRowMapper, parameterSource);
    }

    @Override
    public List<Tags> getAllForVm(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVmId", tagRowMapper, parameterSource);
    }

    /**
     * In the database both TemplateTags and VmTags share the same tables and
     * functions
     * @param ids
     *            the Template ids
     */
    @Override
    public List<Tags> getAllForTemplate(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVmId", tagRowMapper, parameterSource);
    }

    @Override
    public List<Tags> getAllVmTagsWithIds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_ids", ids);

        return getCallsHandler()
                .executeReadList("GetVmTagsByTagId", tagRowMapper, parameterSource);
    }

    @Override
    public List<Tags> getAllForVmPools(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVmpoolId", tagRowMapper, parameterSource);
    }

    @Override
    public void save(Tags tag) {
        Guid id = tag.getTagId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            tag.setTagId(id);
        }

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", tag.getDescription())
                .addValue("tag_id", tag.getTagId())
                .addValue("tag_name", tag.getTagName())
                .addValue("parent_id", tag.getParentId())
                .addValue("readonly", tag.getIsReadonly())
                .addValue("type", tag.getType());

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
    public void update(Tags tag) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", tag.getDescription())
                .addValue("tag_id", tag.getTagId())
                .addValue("tag_name", tag.getTagName())
                .addValue("parent_id", tag.getParentId())
                .addValue("readonly", tag.getIsReadonly())
                .addValue("type", tag.getType());

        getCallsHandler()
                .executeModification("Updatetags", parameterSource);
    }

    @Override
    public TagsUserGroupMap getTagUserGroupByGroupIdAndByTagId(Guid tag, Guid group) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("group_id", group)
                .addValue("tag_id", tag);

        RowMapper<TagsUserGroupMap> mapper = (rs, rowNum) -> {
            TagsUserGroupMap entity = new TagsUserGroupMap();
            entity.setGroupId(getGuidDefaultEmpty(rs, "group_id"));
            entity.setTagId(getGuidDefaultEmpty(rs, "tag_id"));
            return entity;
        };

        return getCallsHandler()
                        .executeRead("GetTagUserGroupByGroupIdAndByTagId", mapper, parameterSource);
    }

    @Override
    public void attachUserGroupToTag(TagsUserGroupMap tagUserGroupMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("group_id",
                tagUserGroupMap.getGroupId()).addValue("tag_id", tagUserGroupMap.getTagId());

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

    @Override
    public TagsUserMap getTagUserByTagIdAndByuserId(Guid tagId, Guid userId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "user_id", userId);

        RowMapper<TagsUserMap> mapper = (rs, rowNum) -> {
            TagsUserMap entity = new TagsUserMap();
            entity.setTagId(getGuidDefaultEmpty(rs, "tag_id"));
            entity.setUserId(getGuidDefaultEmpty(rs, "user_id"));
            return entity;
        };

        return getCallsHandler()
                        .executeRead("GetTagUserByTagIdAndByuserId", mapper, parameterSource);
    }

    @Override
    public void attachUserToTag(TagsUserMap tagUserMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagUserMap.getTagId()).addValue("user_id", tagUserMap.getUserId());

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

    @Override
    public TagsVdsMap getTagVdsByTagIdAndByVdsId(Guid tagId, Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vds_id", vdsId);

        RowMapper<TagsVdsMap> mapper = (rs, rowNum) -> {
            TagsVdsMap entity = new TagsVdsMap();
            entity.setTagId(getGuidDefaultEmpty(rs, "tag_id"));
            entity.setVdsId(getGuidDefaultEmpty(rs, "vds_id"));
            return entity;
        };

        return getCallsHandler()
                        .executeRead("GetTagVdsBytagIdAndByVdsId", mapper, parameterSource);
    }

    @Override
    public void attachVdsToTag(TagsVdsMap tagVdsMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagVdsMap.getTagId()).addValue("vds_id", tagVdsMap.getVdsId());

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

    @Override
    public void detachVdsFromAllTags(Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vds_id", vdsId);

        getCallsHandler()
                .executeModification("DeleteAllTagsVdsMapForHost", parameterSource);
    }

    @Override
    public TagsVmMap getTagVmByTagIdAndByVmId(Guid tagId, Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vm_id", vmId);

        RowMapper<TagsVmMap> mapper = (rs, rowNum) -> {
            TagsVmMap entity = new TagsVmMap();
            entity.setTagId(getGuidDefaultEmpty(rs, "tag_id"));
            entity.setVmId(getGuidDefaultEmpty(rs, "vm_id"));
            entity.setDefaultDisplayType((Integer) rs.getObject("DefaultDisplayType"));
            return entity;
        };

        return getCallsHandler()
                .executeRead("GetTagVmByTagIdAndByvmId", mapper, parameterSource);
    }

    @Override
    public void attachVmToTag(TagsVmMap tagVmMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagVmMap.getTagId()).addValue("vm_id", tagVmMap.getVmId()).addValue("DefaultDisplayType",
                tagVmMap.getDefaultDisplayType());

        getCallsHandler()
                .executeModification("Inserttags_vm_map", parameterSource);
    }

    @Override
    public void updateDefaultDisplayForVmTag(TagsVmMap tagsVmMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagsVmMap.getTagId()).addValue("vm_id", tagsVmMap.getVmId()).addValue("DefaultDisplayType",
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

    @Override
    public List<TagsVmMap> getTagVmMapByVmIdAndDefaultTag(Guid vmid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", vmid);

        RowMapper<TagsVmMap> mapper = (rs, rowNum) -> {
            TagsVmMap entity = new TagsVmMap();
            entity.setTagId(getGuidDefaultEmpty(rs, "tag_id"));
            entity.setVmId(getGuidDefaultEmpty(rs, "vm_id"));
            entity.setDefaultDisplayType((Integer) rs.getObject("DefaultDisplayType"));
            return entity;
        };

        return getCallsHandler()
                        .executeReadList(
                                "GetnVmTagsByVmIdAndDefaultTag", mapper, parameterSource);
    }

    /**
     * In the database both Template and Vm Tags share the same tables and functions
     */
    @Override
    public TagsTemplateMap getTagTemplateByTagIdAndByTemplateId(Guid tagId, Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vm_id", vmId);

        RowMapper<TagsTemplateMap> mapper = (rs, rowNum) -> {
            TagsTemplateMap entity = new TagsTemplateMap();
            entity.setTagId(getGuidDefaultEmpty(rs, "tag_id"));
            entity.setTemplateId(getGuidDefaultEmpty(rs, "vm_id"));
            entity.setDefaultDisplayType((Integer) rs.getObject("DefaultDisplayType"));
            return entity;
        };

        return getCallsHandler()
                .executeRead("GetTagVmByTagIdAndByvmId", mapper, parameterSource);
    }

    @Override
    public void attachTemplateToTag(TagsTemplateMap tagTemplateMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagTemplateMap.getTagId()).addValue("vm_id", tagTemplateMap.getTemplateId()).addValue("DefaultDisplayType",
                tagTemplateMap.getDefaultDisplayType());

        getCallsHandler()
                .executeModification("Inserttags_vm_map", parameterSource);
    }

    @Override
    public void detachTemplateFromTag(Guid tagId, Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vm_id", vmId);

        getCallsHandler()
                .executeModification("Deletetags_vm_map", parameterSource);
    }
}
