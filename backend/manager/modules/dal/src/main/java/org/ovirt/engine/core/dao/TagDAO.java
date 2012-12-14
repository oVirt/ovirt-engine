package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.common.businessentities.TagsUserMap;
import org.ovirt.engine.core.common.businessentities.tags_vds_map;
import org.ovirt.engine.core.common.businessentities.tags_vm_map;
import org.ovirt.engine.core.common.businessentities.tags_vm_pool_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

/**
 * <code>TagDAO</code> defines a type for performing CRUD operations on instances of {@link tags}.
 *
 *
 */
public interface TagDAO extends DAO {

    /**
     * Retrieves the tag with the specified id.
     *
     * @param id
     *            the tag id
     * @return the tag
     */
    tags get(Guid id);

    /**
     * Retrieves the tag with the specified name.
     *
     * @param name
     *            the tag name
     * @return the tag
     */
    tags getByName(String name);

    /**
     * Retrieves all tags.
     *
     * @return the list of tags
     */
    List<tags> getAll();

    /**
     * Retrieves all tags with the given parent id.
     *
     * @param id
     *            the parent id
     * @return the list of tags
     */
    List<tags> getAllForParent(Guid id);

    /**
     * Retrieves the list of tags for the given user group ids.
     *
     * @param ids
     *            the group ids
     * @return the list of tags
     */
    List<tags> getAllForUserGroups(String ids);

    /**
     * Retrieves the list of user group tags by the given ids.
     *
     * @param ids
     *            the ids
     * @return the list of tags
     */
    List<tags> getAllUserGroupTagsWithIds(String ids);

    /**
     * Retrieves the list of tags for the given VDS ids.
     *
     * @param ids
     *            the VDS ids
     * @return the list of tags
     */
    List<tags> getAllForVds(String ids);

    /**
     * Retrieves the list of VDS tags with the given tag ids.
     *
     * @param ids
     *            the tag ids
     * @return the list of tags
     */
    List<tags> getAllForVdsWithIds(String ids);

    /**
     * Retrieves the list of tags for the given VM ids.
     *
     * @param ids
     *            the VM ids
     * @return the list of tags
     */
    List<tags> getAllForVm(String ids);

    /**
     * Retrieves the list of VM tags for the given tag ids.
     *
     * @param ids
     *            the tag ids
     * @return the list of tags
     */
    List<tags> getAllVmTagsWithIds(String ids);

    /**
     * Retrieves the list of tags for the given VM pool ids.
     *
     * @param ids
     *            the pool ids
     * @return the list of tags
     */
    List<tags> getAllForVmPools(String ids);

    /**
     * Retrieves the list of tags for a given user ids.
     *
     * @param ids
     *            the user ids
     * @return the list of tags
     */
    List<tags> getAllForUsers(String ids);

    /**
     * Retrieves the list of user tags for a given tag id.
     *
     * @param ids
     *            the tag ids
     * @return the list of tags
     */
    List<tags> getAllForUsersWithIds(String ids);

    /**
     * Saves the supplied tag.
     *
     * @param tag
     *            the tag
     */
    void save(tags tag);

    /**
     * Updates the supplied tag.
     *
     * @param tag
     *            the tag
     */
    void update(tags tag);

    /**
     * Removes the tag with the specified id.
     *
     * @param id
     */
    void remove(Guid id);

    // TODO these APIs will be eliminated when we move to hibernate

    void attachUserGroupToTag(TagsUserGroupMap map);

    TagsUserGroupMap getTagUserGroupByGroupIdAndByTagId(Guid tagId, Guid groupId);

    void detachUserGroupFromTag(Guid tagId, Guid groupId);

    List<TagsUserGroupMap> getTagUserGroupMapsForTagName(String tagName);

    TagsUserMap getTagUserByTagIdAndByuserId(Guid tagId, Guid userId);

    void attachUserToTag(TagsUserMap tagUserMap);

    void detachUserFromTag(Guid tagId, Guid userId);

    List<TagsUserMap> getTagUserMapByTagName(String tagName);

    tags_vds_map getTagVdsByTagIdAndByVdsId(Guid tagId, Guid vdsId);

    void attachVdsToTag(tags_vds_map tagVdsMap);

    void detachVdsFromTag(Guid tagId, Guid vdsId);

    List<tags_vds_map> getTagVdsMapByTagName(String tagName);

    tags_vm_map getTagVmByTagIdAndByVmId(Guid tagId, Guid vmId);

    void attachVmToTag(tags_vm_map tagVmMap);

    void updateDefaultDisplayForVmTag(tags_vm_map tagsVmMap);

    void detachVmFromTag(Guid tagId, Guid vmId);

    List<tags_vm_map> getTagVmMapByTagName(String tagName);

    List<tags_vm_map> getTagVmMapByVmIdAndDefaultTag(Guid vmid);

    List<tags_vm_map> getTimeLeasedUserVmsByAdGroupAndVmPoolId(Guid adGroupId, Guid vmPoolId);

    List<tags_vm_pool_map> getVmPoolTagsByVmPoolIdAndAdElementId(NGuid vmPoolId, Guid adElementId);
}
