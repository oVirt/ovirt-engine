package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.TagsTemplateMap;
import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.common.businessentities.TagsUserMap;
import org.ovirt.engine.core.common.businessentities.TagsVdsMap;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code TagDao} defines a type for performing CRUD operations on instances of {@link Tags}.
 */
public interface TagDao extends Dao {

    /**
     * Retrieves the tag with the specified id.
     *
     * @param id
     *            the tag id
     * @return the tag
     */
    Tags get(Guid id);

    /**
     * Retrieves the tag with the specified name.
     *
     * @param name
     *            the tag name
     * @return the tag
     */
    Tags getByName(String name);

    /**
     * Retrieves all tags.
     *
     * @return the list of tags
     */
    List<Tags> getAll();

    /**
     * Retrieves all tags with the given parent id.
     *
     * @param id
     *            the parent id
     * @return the list of tags
     */
    List<Tags> getAllForParent(Guid id);

    /**
     * Retrieves the list of tags for the given user group ids.
     *
     * @param ids
     *            the group ids
     * @return the list of tags
     */
    List<Tags> getAllForUserGroups(String ids);

    /**
     * Retrieves the list of tags for the given VDS ids.
     *
     * @param ids
     *            the VDS ids
     * @return the list of tags
     */
    List<Tags> getAllForVds(String ids);

    /**
     * Retrieves the list of tags for the given VM ids.
     *
     * @param ids
     *            the VM ids
     * @return the list of tags
     */
    List<Tags> getAllForVm(String ids);

    /**
     * Retrieves the list of tags for the given Template ids.
     *
     * @param ids
     *            the Template ids
     * @return the list of tags
     */
    List<Tags> getAllForTemplate(String ids);

    /**
     * Retrieves the list of VM tags for the given tag ids.
     *
     * @param ids
     *            the tag ids
     * @return the list of tags
     */
    List<Tags> getAllVmTagsWithIds(String ids);

    /**
     * Retrieves the list of tags for the given VM pool ids.
     *
     * @param ids
     *            the pool ids
     * @return the list of tags
     */
    List<Tags> getAllForVmPools(String ids);

    /**
     * Retrieves the list of tags for a given user ids.
     *
     * @param ids
     *            the user ids
     * @return the list of tags
     */
    List<Tags> getAllForUsers(String ids);

    /**
     * Retrieves the list of user tags for a given tag id.
     *
     * @param ids
     *            the tag ids
     * @return the list of tags
     */
    List<Tags> getAllForUsersWithIds(String ids);

    /**
     * Saves the supplied tag.
     *
     * @param tag
     *            the tag
     */
    void save(Tags tag);

    /**
     * Updates the supplied tag.
     *
     * @param tag
     *            the tag
     */
    void update(Tags tag);

    /**
     * Removes the tag with the specified id.
     */
    void remove(Guid id);

    void attachUserGroupToTag(TagsUserGroupMap map);

    TagsUserGroupMap getTagUserGroupByGroupIdAndByTagId(Guid tagId, Guid groupId);

    void detachUserGroupFromTag(Guid tagId, Guid groupId);

    TagsUserMap getTagUserByTagIdAndByuserId(Guid tagId, Guid userId);

    void attachUserToTag(TagsUserMap tagUserMap);

    void detachUserFromTag(Guid tagId, Guid userId);

    TagsVdsMap getTagVdsByTagIdAndByVdsId(Guid tagId, Guid vdsId);

    void attachVdsToTag(TagsVdsMap tagVdsMap);

    void detachVdsFromTag(Guid tagId, Guid vdsId);

    void detachVdsFromAllTags(Guid vdsId);

    TagsVmMap getTagVmByTagIdAndByVmId(Guid tagId, Guid vmId);

    void attachVmToTag(TagsVmMap tagVmMap);

    void updateDefaultDisplayForVmTag(TagsVmMap tagsVmMap);

    void detachVmFromTag(Guid tagId, Guid vmId);

    List<TagsVmMap> getTagVmMapByVmIdAndDefaultTag(Guid vmid);

    TagsTemplateMap getTagTemplateByTagIdAndByTemplateId(Guid tagId, Guid templateId);

    void attachTemplateToTag(TagsTemplateMap tagVmMap);

    void detachTemplateFromTag(Guid tagId, Guid vmId);
}
