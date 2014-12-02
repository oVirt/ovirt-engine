package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.TagsTemplateMap;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.common.businessentities.TagsUserMap;
import org.ovirt.engine.core.common.businessentities.TagsVdsMap;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.common.businessentities.TagsVmPoolMap;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>TagDAODbFacadeImpl</code> provides an implementation of {@link TagDAO} that uses code refactored from the
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade} class.
 */
@Named
public class TagDAODbFacadeImpl extends BaseDAODbFacade implements TagDAO {
    private static class TagRowMapper implements RowMapper<Tags> {
        public static final TagRowMapper instance = new TagRowMapper();

        @Override
        public Tags mapRow(ResultSet rs, int rowNum) throws SQLException {
            Tags entity = new Tags();
            entity.setdescription(getValueOrNull(rs, "description", ""));
            entity.settag_id(getGuidDefaultNewGuid(rs, "tag_id"));
            entity.settag_name(getValueOrNull(rs, "tag_name", ""));
            entity.setparent_id(getGuidDefaultNewGuid(rs, "parent_id"));
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
    public Tags get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_id", id);

        return getCallsHandler()
                .executeRead("GettagsBytag_id", TagRowMapper.instance, parameterSource);
    }

    @Override
    public Tags getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_name", name);

        return getCallsHandler()
                .executeRead("GettagsBytag_name", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler()
                .executeReadList("GetAllFromtags", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllForParent(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("parent_id", id);

        return getCallsHandler()
                .executeReadList("GettagsByparent_id", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllForUserGroups(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("group_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByUserGroupId", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllUserGroupTagsWithIds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_ids", ids);

        return getCallsHandler()
                .executeReadList("GetUserGroupTagsByTagIds", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllForUsers(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByUserId", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllForUsersWithIds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_ids", ids);

        return getCallsHandler()
                .executeReadList("GetUserTagsByTagIds", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllForVds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVdsId", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllForVdsWithIds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_ids", ids);

        return getCallsHandler()
                .executeReadList("GetVdsTagsByTagIds", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllForVm(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVmId", TagRowMapper.instance, parameterSource);
    }

    /**
     * In the database both TemplateTags and VmTags share the same tables and
     * functions
     * @param ids
     *            the Template ids
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllForTemplate(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVmId", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllVmTagsWithIds(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("tag_ids", ids);

        return getCallsHandler()
                .executeReadList("GetVmTagsByTagId", TagRowMapper.instance, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tags> getAllForVmPools(String ids) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_ids", ids);

        return getCallsHandler()
                .executeReadList("GetTagsByVmpoolId", TagRowMapper.instance, parameterSource);
    }

    @Override
    public void save(Tags tag) {
        Guid id = tag.gettag_id();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
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
    public void update(Tags tag) {
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

        RowMapper<TagsUserGroupMap> mapper = new RowMapper<TagsUserGroupMap>() {
            @Override
            public TagsUserGroupMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsUserGroupMap entity = new TagsUserGroupMap();
                entity.setgroup_id(getGuidDefaultEmpty(rs, "group_id"));
                entity.settag_id(getGuidDefaultEmpty(rs, "tag_id"));
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

    @Override
    public TagsUserMap getTagUserByTagIdAndByuserId(Guid tagId, Guid userId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "user_id", userId);

        RowMapper<TagsUserMap> mapper = new RowMapper<TagsUserMap>() {
            @Override
            public TagsUserMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsUserMap entity = new TagsUserMap();
                entity.settag_id(getGuidDefaultEmpty(rs, "tag_id"));
                entity.setuser_id(getGuidDefaultEmpty(rs, "user_id"));
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

    @Override
    public TagsVdsMap getTagVdsByTagIdAndByVdsId(Guid tagId, Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vds_id", vdsId);

        RowMapper<TagsVdsMap> mapper = new RowMapper<TagsVdsMap>() {
            @Override
            public TagsVdsMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsVdsMap entity = new TagsVdsMap();
                entity.settag_id(getGuidDefaultEmpty(rs, "tag_id"));
                entity.setvds_id(getGuidDefaultEmpty(rs, "vds_id"));
                return entity;
            }
        };

        return getCallsHandler()
                        .executeRead("GetTagVdsBytagIdAndByVdsId", mapper, parameterSource);
    }

    @Override
    public void attachVdsToTag(TagsVdsMap tagVdsMap) {
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

    @Override
    public TagsVmMap getTagVmByTagIdAndByVmId(Guid tagId, Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vm_id", vmId);

        RowMapper<TagsVmMap> mapper = new RowMapper<TagsVmMap>() {
            @Override
            public TagsVmMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsVmMap entity = new TagsVmMap();
                entity.settag_id(getGuidDefaultEmpty(rs, "tag_id"));
                entity.setvm_id(getGuidDefaultEmpty(rs, "vm_id"));
                entity.setDefaultDisplayType((Integer) rs.getObject("DefaultDisplayType"));
                return entity;
            }
        };

        return getCallsHandler()
                .executeRead("GetTagVmByTagIdAndByvmId", mapper, parameterSource);
    }

    @Override
    public void attachVmToTag(TagsVmMap tagVmMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagVmMap.gettag_id()).addValue("vm_id", tagVmMap.getvm_id()).addValue("DefaultDisplayType",
                tagVmMap.getDefaultDisplayType());

        getCallsHandler()
                .executeModification("Inserttags_vm_map", parameterSource);
    }

    @Override
    public void updateDefaultDisplayForVmTag(TagsVmMap tagsVmMap) {
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
    public List<TagsVmMap> getTagVmMapByVmIdAndDefaultTag(Guid vmid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", vmid);

        RowMapper<TagsVmMap> mapper = new RowMapper<TagsVmMap>() {
            @Override
            public TagsVmMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsVmMap entity = new TagsVmMap();
                entity.settag_id(getGuidDefaultEmpty(rs, "tag_id"));
                entity.setvm_id(getGuidDefaultEmpty(rs, "vm_id"));
                entity.setDefaultDisplayType((Integer) rs.getObject("DefaultDisplayType"));
                return entity;
            }
        };

        return getCallsHandler()
                        .executeReadList(
                                "GetnVmTagsByVmIdAndDefaultTag", mapper, parameterSource);
    }

    /**
     * In the database both Template and Vm Tags share the same tables and functions
     * @param tagId
     * @param vmId
     * @return
     */
    @Override
    public TagsTemplateMap getTagTemplateByTagIdAndByTemplateId(Guid tagId, Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id", tagId).addValue(
                "vm_id", vmId);

        RowMapper<TagsTemplateMap> mapper = new RowMapper<TagsTemplateMap>() {
            @Override
            public TagsTemplateMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsTemplateMap entity = new TagsTemplateMap();
                entity.settag_id(getGuidDefaultEmpty(rs, "tag_id"));
                entity.settemplate_id(getGuidDefaultEmpty(rs, "vm_id"));
                entity.setDefaultDisplayType((Integer) rs.getObject("DefaultDisplayType"));
                return entity;
            }
        };

        return getCallsHandler()
                .executeRead("GetTagVmByTagIdAndByvmId", mapper, parameterSource);
    }

    @Override
    public void attachTemplateToTag(TagsTemplateMap tagTemplateMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("tag_id",
                tagTemplateMap.gettag_id()).addValue("vm_id", tagTemplateMap.gettemplate_id()).addValue("DefaultDisplayType",
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

    @SuppressWarnings("unchecked")
    @Override
    public List<TagsVmPoolMap> getVmPoolTagsByVmPoolIdAndAdElementId(Guid vmPoolId, Guid adElementId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("ad_id", adElementId)
                .addValue("vm_pool_id", vmPoolId);

        RowMapper<TagsVmPoolMap> mapper = new RowMapper<TagsVmPoolMap>() {
            @Override
            public TagsVmPoolMap mapRow(ResultSet rs, int rowNum) throws SQLException {
                TagsVmPoolMap entity = new TagsVmPoolMap();
                entity.settag_id(getGuidDefaultEmpty(rs, "tag_id"));
                entity.setvm_pool_id(getGuidDefaultEmpty(rs, "vm_pool_id"));
                return entity;
            }
        };

        return getCallsHandler()
                        .executeReadList(
                                "GetVmPoolTagsByVmPoolIdAndAdElementId", mapper, parameterSource);
    }
}
