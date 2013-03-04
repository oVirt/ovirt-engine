package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.common.businessentities.TagsUserMap;
import org.ovirt.engine.core.common.businessentities.TagsVdsMap;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>TagDAOTest</code> provides unit tests to validate the functionality for {@link TagDAO}.
 *
 *
 */
public class TagDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_TAG_ID = new Guid("d3ec3e01-ca89-48e1-8b43-a9b38f873b0c");
    private static final int TAG_COUNT = 3;
    private static final Guid EXISTING_GROUP_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid EXISTING_USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
    private static final Guid EXISTING_VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid FREE_VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7");
    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid FREE_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private TagDAO dao;
    private tags newTag;
    private tags existingTag;
    private Guid parent;
    private Guid user;
    private Guid vm;
    private TagsUserGroupMap existingUserGroupTag;
    private TagsUserMap existingUserTag;
    private TagsVdsMap existingVdsTag;
    private TagsVdsMap newVdsTag;
    private TagsVmMap existingVmTag;
    private TagsVmMap newVmTag;
    private Guid vmPool;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getTagDao();

        existingTag = dao.get(EXISTING_TAG_ID);
        existingTag = dao.get(new Guid("d3ec3e01-ca89-48e1-8b43-a9b38f873b0c"));
        parent = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");
        user = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
        vm = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
        vmPool = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0076");

        newTag = new tags();

        newTag.settag_name("newtagname");
        newTag.setIsReadonly(true);
        newTag.setdescription("newtagdescription");
        newTag.setparent_id(parent);

        existingUserGroupTag = dao.getTagUserGroupByGroupIdAndByTagId(EXISTING_TAG_ID, EXISTING_GROUP_ID);

        existingUserTag = dao.getTagUserByTagIdAndByuserId(EXISTING_TAG_ID, EXISTING_USER_ID);

        existingVdsTag = dao.getTagVdsByTagIdAndByVdsId(EXISTING_TAG_ID, EXISTING_VDS_ID);
        newVdsTag = new TagsVdsMap(EXISTING_TAG_ID, FREE_VDS_ID);

        existingVmTag = dao.getTagVmByTagIdAndByVmId(EXISTING_TAG_ID, EXISTING_VM_ID);
        newVmTag = new TagsVmMap(EXISTING_TAG_ID, FREE_VM_ID);
    }

    /**
     * Ensures that using an invalid id returns no tag.
     */
    @Test
    public void testGetWithInvalidId() {
        tags result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that getting a tag by id works as expected.
     */
    @Test
    public void testGet() {
        tags result = dao.get(existingTag.gettag_id());

        assertNotNull(result);
        assertEquals(existingTag, result);
    }

    /**
     * Ensures that using an invalid name returns no tag.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        tags result = dao.getByName("invalidtagname");

        assertNull(result);
    }

    /**
     * Ensures that retrieving a tag by name works as expected.
     */
    @Test
    public void testGetByName() {
        tags result = dao.getByName(existingTag.gettag_name());

        assertNotNull(result);
        assertEquals(existingTag, result);
    }

    @Test
    public void testGetAll() {
        List<tags> result = dao.getAll();

        assertNotNull(result);
        assertEquals(TAG_COUNT, result.size());
    }

    /**
     * Ensures that getting all tags for a parent with no tags returns an empty collection.
     */
    @Test
    public void testGetAllForParentWithInvalidParent() {
        List<tags> result = dao.getAllForParent(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that getting all tags for a parent works as expected.
     */
    @Test
    public void testGetAllForParent() {
        List<tags> result = dao.getAllForParent(parent);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (tags tag : result) {
            assertEquals(parent, tag.getparent_id());
        }
    }

    /**
     * Ensures that an empty collection is returned when the specified user group has no tags.
     */
    @Test
    public void testGetAllForUserGroupWithInvalidUserGroup() {
        List<tags> result = dao.getAllForUserGroups(Guid.NewGuid().toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that all tags for the specified user group are returned.
     */
    @Test
    public void testGetAllForUserGroup() {
        List<tags> result = dao
                .getAllForUserGroups("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

        assertNotNull(result);
        assertFalse(result.isEmpty());

        //FIXME: Fix this test - userGroup is not set
        /*

        Guid userGroupId = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

        for (tags tag : result) {
            assertEquals(userGroupId, tag.getUserGroup().getid());
        }*/
    }

    /**
     * Ensures that getting all tags for a user with no tags returns an empty collection.
     */
    @Test
    public void testGetAllForUserWithInvalidUser() {
        List<tags> result = dao.getAllForUsers(Guid.NewGuid().toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right set of tags are returned for a specified user.
     */
    @Test
    public void testGetAllForUser() {
        List<tags> result = dao.getAllForUsers(user.getUuid().toString());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForUserIdsWithInvalidIds() {
        List<tags> result = dao.getAllForUsersWithIds(Guid.NewGuid().getUuid()
                .toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a collection of tags are returned.
     */
    @Test
    public void testGetAllForUserIds() {
        List<tags> result = dao
                .getAllForUsersWithIds("d3ec3e01-ca89-48e1-8b43-a9b38f873b0c");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Asserts that an VDS with no tags returns an empty collection.
     */
    @Test
    public void testGetAllForVdsWithInvalidVds() {
        List<tags> result = dao.getAllForVds(Guid.NewGuid().getUuid()
                .toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a set of tags are returned.
     */
    @Test
    public void testGetAllForVds() {
        List<tags> result = dao
                .getAllForVds("afce7a39-8e8c-4819-ba9c-796d316592e6");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForVmWithInvalidVm() {
        List<tags> result = dao
                .getAllForVm(Guid.NewGuid().getUuid().toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a collection of tags is returned.
     */
    @Test
    public void testGetAllForVm() {
        List<tags> result = dao.getAllForVm(vm.getUuid().toString());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all tags from the given list that have VMs associated with them are returned.
     */
    @Test
    public void testGetAllVmTagsWithIds() {
        List<tags> result = dao.getAllVmTagsWithIds(existingTag.gettag_id().toString());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all tags for the specified VM pool are returned.
     */
    @Test
    public void testGetAllForVmPools() {
        List<tags> result = dao.getAllForVmPools(vmPool.toString());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that saving a tag works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newTag);

        tags result = dao.get(newTag.gettag_id());

        assertNotNull(result);
        assertEquals(newTag, result);
    }

    /**
     * Ensures that updating a tag in the database works as expected.
     */
    @Test
    public void testUpdate() {
        existingTag.setdescription("this is the updated description!");

        dao.update(existingTag);

        tags result = dao.get(existingTag.gettag_id());

        assertEquals(existingTag, result);
    }

    /**
     * Ensures that removing a tag works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingTag.gettag_id());

        tags result = dao.get(existingTag.gettag_id());

        assertNull(result);
    }

    @Test
    public void testGetTagUserGroupByGroupIdAndTagId() {
        TagsUserGroupMap result = dao.getTagUserGroupByGroupIdAndByTagId(existingUserGroupTag.gettag_id(),
                        existingUserGroupTag.getgroup_id());

        assertNotNull(result);
        assertEqualsTagUserGroupMap(existingUserGroupTag,result);

    }

    private void assertEqualsTagUserGroupMap(TagsUserGroupMap existing, TagsUserGroupMap result) {
        assertEquals("Group IDs not equal",existing.getgroup_id(), result.getgroup_id());
        assertEquals("Tag IDs not equal",existing.gettag_id(), result.gettag_id());
        assertEquals("Object equation",existing,result);

    }

    @Test
    public void testDetachUserGroupFromTag() {
        dao.detachUserGroupFromTag(existingUserGroupTag.gettag_id(), existingUserGroupTag.getgroup_id());

        TagsUserGroupMap result = dao.getTagUserGroupByGroupIdAndByTagId(existingUserGroupTag.gettag_id(),
                existingUserGroupTag.getgroup_id());

        assertNull(result);
    }

    @Test
    public void testGetUserTag() {
        TagsUserMap result =
                dao.getTagUserByTagIdAndByuserId(existingUserTag.gettag_id(), existingUserTag.getuser_id());

        assertNotNull(result);
        assertEqualsTagUserMap(existingUserTag,result);
    }

    private void assertEqualsTagUserMap(TagsUserMap existing, TagsUserMap result) {
        assertEquals("Tag ID is not equal",existing.gettag_id(), result.gettag_id());
        assertEquals("USER ID is not equal",existing.getuser_id(), result.getuser_id());
        assertEquals("Object equation",existing,result);
    }

    @Test
    public void testGetTagVdsMap() {
        TagsVdsMap result = dao.getTagVdsByTagIdAndByVdsId(existingVdsTag.gettag_id(), existingVdsTag.getvds_id());

        assertNotNull(result);
        assertEqualsTagsVdsMap(existingVdsTag,result);
    }

    private void assertEqualsTagsVdsMap(TagsVdsMap existing, TagsVdsMap result) {
        assertEquals("Tag ID is not equal",existing.gettag_id(), result.gettag_id());
        assertEquals("VDS ID is not equal",existing.getvds_id(), result.getvds_id());
        assertEquals("Object equation",existing,result);
    }

    @Test
    public void testAttachVdsToTag() {
        dao.attachVdsToTag(newVdsTag);

        TagsVdsMap result = dao.getTagVdsByTagIdAndByVdsId(newVdsTag.gettag_id(), newVdsTag.getvds_id());

        assertNotNull(result);
        assertEqualsTagsVdsMap(newVdsTag,result);
    }

    @Test
    public void testDetachVdsFromTag() {
        dao.detachVdsFromTag(existingVdsTag.gettag_id(), existingVdsTag.getvds_id());

        TagsVdsMap result = dao.getTagVdsByTagIdAndByVdsId(existingVdsTag.gettag_id(), existingVdsTag.getvds_id());

        assertNull(result);
    }

    @Test
    public void testGetTagVdsMapByTagName() {
        List<TagsVdsMap> result = dao.getTagVdsMapByTagName(existingTag.gettag_name());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (TagsVdsMap mapping : result) {
            assertEquals(existingTag.gettag_id(), mapping.gettag_id());
        }
    }

    @Test
    public void testGetTagVmMapByTag() {
        TagsVmMap result = dao.getTagVmByTagIdAndByVmId(existingVmTag.gettag_id(), existingVmTag.getvm_id());

        assertNotNull(result);
        assertEqualsTagsVmMap(existingVmTag, result);
    }

    private void assertEqualsTagsVmMap(TagsVmMap existing, TagsVmMap result) {
        assertEquals("TG is not equal",existing.gettag_id(),result.gettag_id());
        assertEquals("VM id not equal ",existing.getvm_id(),result.getvm_id());
        assertEquals("Object equation",existing,result);
    }

    @Test
    public void testAttachVmToTag() {
        dao.attachVmToTag(newVmTag);

        TagsVmMap result = dao.getTagVmByTagIdAndByVmId(newVmTag.gettag_id(), newVmTag.getvm_id());

        assertNotNull(result);
        assertEqualsTagsVmMap(newVmTag, result);
    }

    @Test
    public void updateVmTag() {
        existingVmTag.setDefaultDisplayType(existingVmTag.getDefaultDisplayType() + 1);

        dao.updateDefaultDisplayForVmTag(existingVmTag);

        TagsVmMap result = dao.getTagVmByTagIdAndByVmId(existingVmTag.gettag_id(), existingVmTag.getvm_id());

        assertNotNull(result);
        assertEqualsTagsVmMap(existingVmTag, result);
    }

    @Test
    public void testDetachVmFromTag() {
        dao.detachVmFromTag(existingVmTag.gettag_id(), existingVmTag.getvm_id());

        TagsVmMap result = dao.getTagVmByTagIdAndByVmId(existingVmTag.gettag_id(), existingVmTag.getvm_id());

        assertNull(result);
    }

    @Test
    public void testGetTagVmMapByTagName() {
        List<TagsVmMap> result = dao.getTagVmMapByTagName(existingTag.gettag_name());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (TagsVmMap mapping : result) {
            assertEquals(existingTag.gettag_id(), mapping.gettag_id());
        }
    }

    @Test
    public void testGetTagVmMapByByVmIdAndDefaultTag() {
        List<TagsVmMap> result = dao.getTagVmMapByVmIdAndDefaultTag(EXISTING_VM_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (TagsVmMap mapping : result) {
            assertEquals(EXISTING_VM_ID, mapping.getvm_id());
        }
    }
}
