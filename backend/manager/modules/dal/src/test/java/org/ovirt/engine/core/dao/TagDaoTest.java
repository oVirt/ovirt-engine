package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.TagsTemplateMap;
import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.common.businessentities.TagsUserMap;
import org.ovirt.engine.core.common.businessentities.TagsVdsMap;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>TagDaoTest</code> provides unit tests to validate the functionality for {@link TagDao}.
 *
 *
 */
public class TagDaoTest extends BaseDaoTestCase {
    private static final Guid EXISTING_TAG_ID = new Guid("d3ec3e01-ca89-48e1-8b43-a9b38f873b0c");
    private static final int TAG_COUNT = 3;
    private static final Guid EXISTING_GROUP_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid EXISTING_USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
    private static final Guid EXISTING_VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid FREE_VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7");
    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid EXISTING_TEMPLATE_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid FREE_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final Guid FREE_TEMPLATE_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private TagDao dao;
    private Tags newTag;
    private Tags existingTag;
    private Guid parent;
    private Guid user;
    private Guid vm;
    private Guid template;
    private TagsUserGroupMap existingUserGroupTag;
    private TagsUserMap existingUserTag;
    private TagsVdsMap existingVdsTag;
    private TagsVdsMap newVdsTag;
    private TagsVmMap existingVmTag;
    private TagsTemplateMap existingTemplateTag;
    private TagsVmMap newVmTag;
    private TagsTemplateMap newTemplateTag;
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
        template = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
        vmPool = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0076");

        newTag = new Tags();

        newTag.settag_name("newtagname");
        newTag.setIsReadonly(true);
        newTag.setdescription("newtagdescription");
        newTag.setparent_id(parent);

        existingUserGroupTag = dao.getTagUserGroupByGroupIdAndByTagId(EXISTING_TAG_ID, EXISTING_GROUP_ID);

        existingUserTag = dao.getTagUserByTagIdAndByuserId(EXISTING_TAG_ID, EXISTING_USER_ID);

        existingVdsTag = dao.getTagVdsByTagIdAndByVdsId(EXISTING_TAG_ID, EXISTING_VDS_ID);
        newVdsTag = new TagsVdsMap(EXISTING_TAG_ID, FREE_VDS_ID);

        existingVmTag = dao.getTagVmByTagIdAndByVmId(EXISTING_TAG_ID, EXISTING_VM_ID);
        existingTemplateTag = dao.getTagTemplateByTagIdAndByTemplateId(EXISTING_TAG_ID, EXISTING_TEMPLATE_ID);
        newVmTag = new TagsVmMap(EXISTING_TAG_ID, FREE_VM_ID);
        newTemplateTag = new TagsTemplateMap(EXISTING_TAG_ID, FREE_TEMPLATE_ID);
    }

    /**
     * Ensures that using an invalid id returns no tag.
     */
    @Test
    public void testGetWithInvalidId() {
        Tags result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that getting a tag by id works as expected.
     */
    @Test
    public void testGet() {
        Tags result = dao.get(existingTag.gettag_id());

        assertNotNull(result);
        assertEquals(existingTag, result);
    }

    /**
     * Ensures that using an invalid name returns no tag.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        Tags result = dao.getByName("invalidtagname");

        assertNull(result);
    }

    /**
     * Ensures that retrieving a tag by name works as expected.
     */
    @Test
    public void testGetByName() {
        Tags result = dao.getByName(existingTag.gettag_name());

        assertNotNull(result);
        assertEquals(existingTag, result);
    }

    @Test
    public void testGetAll() {
        List<Tags> result = dao.getAll();

        assertNotNull(result);
        assertEquals(TAG_COUNT, result.size());
    }

    /**
     * Ensures that getting all tags for a parent with no tags returns an empty collection.
     */
    @Test
    public void testGetAllForParentWithInvalidParent() {
        List<Tags> result = dao.getAllForParent(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that getting all tags for a parent works as expected.
     */
    @Test
    public void testGetAllForParent() {
        List<Tags> result = dao.getAllForParent(parent);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Tags tag : result) {
            assertEquals(parent, tag.getparent_id());
        }
    }

    /**
     * Ensures that an empty collection is returned when the specified user group has no tags.
     */
    @Test
    public void testGetAllForUserGroupWithInvalidUserGroup() {
        List<Tags> result = dao.getAllForUserGroups(Guid.newGuid().toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that all tags for the specified user group are returned.
     */
    @Test
    public void testGetAllForUserGroup() {
        List<Tags> result = dao
                .getAllForUserGroups("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

        assertNotNull(result);
        assertFalse(result.isEmpty());

        //FIXME: Fix this test - userGroup is not set
        /*

        Guid userGroupId = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

        for (tags tag : result) {
            assertEquals(userGroupId, tag.getUserGroup().getId());
        }*/
    }

    /**
     * Ensures that getting all tags for a user with no tags returns an empty collection.
     */
    @Test
    public void testGetAllForUserWithInvalidUser() {
        List<Tags> result = dao.getAllForUsers(Guid.newGuid().toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right set of tags are returned for a specified user.
     */
    @Test
    public void testGetAllForUser() {
        List<Tags> result = dao.getAllForUsers(user.getUuid().toString());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForUserIdsWithInvalidIds() {
        List<Tags> result = dao.getAllForUsersWithIds(Guid.newGuid().getUuid()
                .toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a collection of tags are returned.
     */
    @Test
    public void testGetAllForUserIds() {
        List<Tags> result = dao
                .getAllForUsersWithIds("d3ec3e01-ca89-48e1-8b43-a9b38f873b0c");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Asserts that an VDS with no tags returns an empty collection.
     */
    @Test
    public void testGetAllForVdsWithInvalidVds() {
        List<Tags> result = dao.getAllForVds(Guid.newGuid().getUuid()
                .toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a set of tags are returned.
     */
    @Test
    public void testGetAllForVds() {
        List<Tags> result = dao
                .getAllForVds("afce7a39-8e8c-4819-ba9c-796d316592e6");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForVmWithInvalidVm() {
        List<Tags> result = dao
                .getAllForVm(Guid.newGuid().getUuid().toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    /**
     * Ensures that a collection of tags is returned.
     */
    @Test
    public void testGetAllForVm() {
        List<Tags> result = dao.getAllForTemplate(template.getUuid().toString());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForVmWithInvalidTemplate() {
        List<Tags> result = dao
                .getAllForTemplate(Guid.newGuid().getUuid().toString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a collection of tags is returned.
     */
    @Test
    public void testGetAllForTemplate() {
        List<Tags> result = dao.getAllForTemplate(vm.getUuid().toString());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all tags from the given list that have VMs associated with them are returned.
     */
    @Test
    public void testGetAllVmTagsWithIds() {
        List<Tags> result = dao.getAllVmTagsWithIds(existingTag.gettag_id().toString());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all tags for the specified VM pool are returned.
     */
    @Test
    public void testGetAllForVmPools() {
        List<Tags> result = dao.getAllForVmPools(vmPool.toString());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that saving a tag works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newTag);

        Tags result = dao.get(newTag.gettag_id());

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

        Tags result = dao.get(existingTag.gettag_id());

        assertEquals(existingTag, result);
    }

    /**
     * Ensures that removing a tag works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingTag.gettag_id());

        Tags result = dao.get(existingTag.gettag_id());

        assertNull(result);
    }

    @Test
    public void testGetTagUserGroupByGroupIdAndTagId() {
        TagsUserGroupMap result = dao.getTagUserGroupByGroupIdAndByTagId(existingUserGroupTag.gettag_id(),
                        existingUserGroupTag.getgroup_id());

        assertNotNull(result);
        assertEqualsTagUserGroupMap(existingUserGroupTag, result);

    }

    private void assertEqualsTagUserGroupMap(TagsUserGroupMap existing, TagsUserGroupMap result) {
        assertEquals("Group IDs not equal", existing.getgroup_id(), result.getgroup_id());
        assertEquals("Tag IDs not equal", existing.gettag_id(), result.gettag_id());
        assertEquals("Object equation", existing, result);

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
        assertEqualsTagUserMap(existingUserTag, result);
    }

    private void assertEqualsTagUserMap(TagsUserMap existing, TagsUserMap result) {
        assertEquals("Tag ID is not equal", existing.gettag_id(), result.gettag_id());
        assertEquals("USER ID is not equal", existing.getuser_id(), result.getuser_id());
        assertEquals("Object equation", existing, result);
    }

    @Test
    public void testGetTagVdsMap() {
        TagsVdsMap result = dao.getTagVdsByTagIdAndByVdsId(existingVdsTag.gettag_id(), existingVdsTag.getvds_id());

        assertNotNull(result);
        assertEqualsTagsVdsMap(existingVdsTag, result);
    }

    private void assertEqualsTagsVdsMap(TagsVdsMap existing, TagsVdsMap result) {
        assertEquals("Tag ID is not equal", existing.gettag_id(), result.gettag_id());
        assertEquals("VDS ID is not equal", existing.getvds_id(), result.getvds_id());
        assertEquals("Object equation", existing, result);
    }

    @Test
    public void testAttachVdsToTag() {
        dao.attachVdsToTag(newVdsTag);

        TagsVdsMap result = dao.getTagVdsByTagIdAndByVdsId(newVdsTag.gettag_id(), newVdsTag.getvds_id());

        assertNotNull(result);
        assertEqualsTagsVdsMap(newVdsTag, result);
    }

    @Test
    public void testDetachVdsFromTag() {
        dao.detachVdsFromTag(existingVdsTag.gettag_id(), existingVdsTag.getvds_id());

        TagsVdsMap result = dao.getTagVdsByTagIdAndByVdsId(existingVdsTag.gettag_id(), existingVdsTag.getvds_id());

        assertNull(result);
    }

    @Test
    public void testGetTagVmMapByTag() {
        TagsVmMap result = dao.getTagVmByTagIdAndByVmId(existingVmTag.gettag_id(), existingVmTag.getvm_id());

        assertNotNull(result);
        assertEqualsTagsVmMap(existingVmTag, result);
    }

    @Test
    public void testGetTagTemplateMapByTag() {
        TagsTemplateMap result = dao.getTagTemplateByTagIdAndByTemplateId(existingTemplateTag.gettag_id(), existingTemplateTag.gettemplate_id());

        assertNotNull(result);
        assertEqualsTagsTemplateMap(existingTemplateTag, result);
    }

    private void assertEqualsTagsVmMap(TagsVmMap existing, TagsVmMap result) {
        assertEquals("TG is not equal", existing.gettag_id(), result.gettag_id());
        assertEquals("VM id not equal ", existing.getvm_id(), result.getvm_id());
        assertEquals("Object equation", existing, result);
    }

    private void assertEqualsTagsTemplateMap(TagsTemplateMap existing, TagsTemplateMap result) {
        assertEquals("TG is not equal", existing.gettag_id(), result.gettag_id());
        assertEquals("Template id not equal ", existing.gettemplate_id(), result.gettemplate_id());
        assertEquals("Object equation", existing, result);
    }

    @Test
    public void testAttachVmToTag() {
        dao.attachVmToTag(newVmTag);

        TagsVmMap result = dao.getTagVmByTagIdAndByVmId(newVmTag.gettag_id(), newVmTag.getvm_id());

        assertNotNull(result);
        assertEqualsTagsVmMap(newVmTag, result);
    }

    @Test
    public void testAttachTemplateToTag() {
        dao.attachTemplateToTag(newTemplateTag);

        TagsTemplateMap result = dao.getTagTemplateByTagIdAndByTemplateId(newTemplateTag.gettag_id(), newTemplateTag.gettemplate_id());

        assertNotNull(result);
        assertEqualsTagsTemplateMap(newTemplateTag, result);
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
    public void testDetachTemplateFromTag() {
        dao.detachTemplateFromTag(existingTemplateTag.gettag_id(), existingTemplateTag.gettemplate_id());

        TagsTemplateMap result = dao.getTagTemplateByTagIdAndByTemplateId(existingTemplateTag.gettag_id(), existingTemplateTag.gettemplate_id());

        assertNull(result);
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
