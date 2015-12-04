package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.TransformerUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.TagDao;

@RunWith(MockitoJUnitRunner.class)
public class TagsDirectorTest {

    private TagsDirector tagsDirector;

    @Mock
    private TagDao tagDao;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("jboss.server.log.dir", "/tmp");
    }

    @AfterClass
    public static void afterClass() {
        File f = new File("/tmp/engine.log");
        f.delete();
    }

    @Before
    public void setup() {
        tagsDirector = spy(TagsDirector.getInstance());
        when(tagDao.getAllForParent(any(Guid.class))).thenReturn(Collections.<Tags> emptyList());
        doReturn(tagDao).when(tagsDirector).getTagDao();
        doNothing().when(tagsDirector).updateTagInBackend(any(Tags.class));
        tagsDirector.init();
    }

    protected Tags createTag(String name, String description) {
        Guid tagId = Guid.newGuid();
        Tags tag = new Tags();
        tag.setdescription(description);
        tag.settag_id(tagId);
        tag.settag_name(name);
        tag.settype(TagsType.GeneralTag);
        tag.setparent_id(tagsDirector.getRootTag().gettag_id());
        return tag;
    }

    @Test
    public void testCloneTag() {
        Tags tag = createTag("a", "b");
        Tags tag2 = createTag("c", "d");
        tag.getChildren().add(tag2);
        tag2.setparent_id(tag.gettag_id());
        Transformer cloner = TransformerUtils.cloneTransformer();
        Tags newTag = (Tags) cloner.transform(tag);
        assertEquals(1, newTag.getChildren().size());
    }

    @Test
    public void testAddTag() {
        Tags tag = createTag("tag1", "desc1");

        Tags tagFromDirector = tagsDirector.getTagById(tag.gettag_id());
        assertNull(tagFromDirector);

        tagsDirector.addTag(tag);
        tagFromDirector = tagsDirector.getTagById(tag.gettag_id());
        assertNotNull(tagFromDirector);
        assertEquals(tag, tagFromDirector);
    }

    @Test
    public void testChangeValueAfterAdd() {
        Tags tag = createTag("tag1", "desc1");
        tagsDirector.addTag(tag);
        tag.settag_name("tag2");
        Tags tagFromDirector = tagsDirector.getTagById(tag.gettag_id());
        assertNotNull(tagFromDirector);
        assertEquals("tag1", tagFromDirector.gettag_name());
    }

    @Test
    public void testGetRootTag() {
        Tags tags = tagsDirector.getRootTag();
        assertNotNull(tags);
    }

    @Test
    public void testGetRootWithHierarchy() {
        Tags tag = createTag("tag1", "desc1");
        tagsDirector.addTag(tag);
        Tags tags = tagsDirector.getRootTag();
        assertNotNull(tags);
        assertEquals(tags.gettag_id(), TagsDirector.ROOT_TAG_ID);
        assertEquals(1, tags.getChildren().size());
        assertEquals("tag1", tags.getChildren().get(0).gettag_name());
    }

    @Test
    public void testUpdateChildren() {
        Tags tag = createTag("tag1", "desc1");
        tagsDirector.addTag(tag);
        tag.settag_name("booboo");
        tagsDirector.updateTag(tag);
        Tags rootTag = tagsDirector.getRootTag();
        tag = rootTag.getChildren().get(0);
        assertEquals("booboo", tag.gettag_name());
    }

    @Test
    public void testMoveTag() {
        // let's have two top level tag under root
        Tags level1obj1 = createTag("level1obj1", "");
        level1obj1.settag_id(Guid.newGuid());
        level1obj1.setparent_id(tagsDirector.getRootTag().gettag_id());
        tagsDirector.addTag(level1obj1);
        Tags level1obj2 = createTag("level1obj2", "");
        level1obj2.settag_id(Guid.newGuid());
        level1obj2.setparent_id(tagsDirector.getRootTag().gettag_id());
        tagsDirector.addTag(level1obj2);

        // now none of these should have any children
        Assert.assertEquals(0, tagsDirector.getTagById(level1obj1.gettag_id()).getChildren().size());
        Assert.assertEquals(0, tagsDirector.getTagById(level1obj2.gettag_id()).getChildren().size());

        // now let's add a child tag o the first top level tag
        Tags level2obj1 = createTag("level2obj1", "");
        level2obj1.settag_id(Guid.newGuid());
        level2obj1.setparent_id(level1obj1.gettag_id());
        tagsDirector.addTag(level2obj1);

        // now check the number of children
        Assert.assertEquals(1, tagsDirector.getTagById(level1obj1.gettag_id()).getChildren().size());
        Assert.assertEquals(0, tagsDirector.getTagById(level1obj2.gettag_id()).getChildren().size());

        // should be all right so far.
        // now let's do the trick: move the second level tag to under the other first level tag
        tagsDirector.moveTag(level2obj1.gettag_id(), level1obj2.gettag_id());

        // and now let's recheck, the first top level should have 0 children, the second should have 1
        Assert.assertEquals(0, tagsDirector.getTagById(level1obj1.gettag_id()).getChildren().size());
        Assert.assertEquals(1, tagsDirector.getTagById(level1obj2.gettag_id()).getChildren().size());

    }

    @Test
    public void testMoveTagRoot() {
        // let's have two top level tag under root
        Tags level1obj1 = createTag("level1obj1", "");
        level1obj1.settag_id(Guid.newGuid());
        level1obj1.setparent_id(tagsDirector.getRootTag().gettag_id());
        tagsDirector.addTag(level1obj1);
        Tags level1obj2 = createTag("level1obj2", "");
        level1obj2.settag_id(Guid.newGuid());
        level1obj2.setparent_id(tagsDirector.getRootTag().gettag_id());
        tagsDirector.addTag(level1obj2);

        // now none of these should have any children
        Assert.assertEquals(0, tagsDirector.getTagById(level1obj1.gettag_id()).getChildren().size());
        Assert.assertEquals(0, tagsDirector.getTagById(level1obj2.gettag_id()).getChildren().size());
        Assert.assertEquals(2, tagsDirector.getRootTag().getChildren().size());

        // should be all right so far.
        // now let's do the trick: move the second level tag to under the other first level tag
        tagsDirector.moveTag(level1obj1.gettag_id(), level1obj2.gettag_id());

        // and now let's recheck, the first top level should have 0 children, the second should have 1
        Assert.assertEquals(1, tagsDirector.getTagById(level1obj2.gettag_id()).getChildren().size());
        Assert.assertEquals(1, tagsDirector.getRootTag().getChildren().size());

    }

    @Test
    public void testUpdateParentTag() {
        Tags tag = createTag("tag1", "desc1");
        tag.settag_id(Guid.newGuid());
        tagsDirector.addTag(tag);
        Tags rootTag = tagsDirector.getRootTag();
        tag = rootTag.getChildren().get(0);
        assertEquals("tag1", tag.gettag_name());

        // now let's add another tag
        Tags sub = createTag("subtag1", "subdesc");
        sub.settag_id(Guid.newGuid());
        sub.setparent_id(tag.gettag_id());
        tagsDirector.addTag(sub);

        // so now the root tag must have 1 child
        Assert.assertEquals(1, tagsDirector.getRootTag().getChildren().size());
        Assert.assertEquals(1, tagsDirector.getTagById(tag.gettag_id()).getChildren().size());

        // get the parent, and rename it
        tag.settag_name("subtag1_up");
        tagsDirector.updateTag(tag);

        // now let's see the number of children in the tag objects
        // this is the assertion that fails without fix for #732640
        Assert.assertEquals(1, tagsDirector.getRootTag().getChildren().size());
        Assert.assertEquals(1, tagsDirector.getTagById(tag.gettag_id()).getChildren().size());

        // let's check the same thing on overwriting description
        tag.setdescription("TEST TEST TEST TEST");
        tagsDirector.updateTag(tag);

        // and all the checks once again just to make sure
        Assert.assertEquals(1, tagsDirector.getRootTag().getChildren().size());
        Assert.assertEquals(1, tagsDirector.getTagById(tag.gettag_id()).getChildren().size());

    }

    @Test
    public void testGetTagByNameNotExists() {
        Tags fromTagsDirector = tagsDirector.getTagByName("does not exist");
        assertNull(fromTagsDirector);
    }

    @Test
    public void testGetByName() {
        Tags tag = createTag("tag1", "desc1");
        tagsDirector.addTag(tag);
        Tags fromTagsDirector = tagsDirector.getTagByName("tag1");
        assertNotNull(fromTagsDirector);
        assertEquals(tag, fromTagsDirector);
    }

    @Test
    public void testGetAllTags() {
        ArrayList<Tags> allTags = tagsDirector.getAllTags();
        assertEquals(0, allTags.size());
        Tags tag = createTag("tag1", "desc1");
        tagsDirector.addTag(tag);
        allTags = tagsDirector.getAllTags();
        assertEquals(1, allTags.size());
        tag = createTag("tag2", "desc2");
        tagsDirector.addTag(tag);
        allTags = tagsDirector.getAllTags();
        assertEquals(2, allTags.size());
    }

    @Test
    public void testGetTagIdAndChildrenIdsNotExists() {
        Tags tag = createTag("tag1", "desc1");
        String idsStr = tagsDirector.getTagIdAndChildrenIds(tag.gettag_id());
        assertEquals(StringUtils.EMPTY, idsStr);
    }

    @Test
    public void testGetTagIdAndChildrenIds() {
        Tags tag = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        tag.getChildren().add(tag2);
        tag2.setparent_id(tag.getparent_id());
        tagsDirector.addTag(tag);
        String idsStr = tagsDirector.getTagIdAndChildrenIds(tag.gettag_id());
        String[] ids = idsStr.split("[,]");
        assertEquals(2, ids.length);
        assertEquals(ids[0], "'" + tag.gettag_id().toString() + "'");
        assertEquals(ids[1], "'" + tag2.gettag_id().toString() + "'");
    }

    @Test
    public void testGetTagIdAndChildrenIdsAsSet() {
        Tags tag = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        tag.getChildren().add(tag2);
        tag2.setparent_id(tag.getparent_id());
        tagsDirector.addTag(tag);
        Set<Guid> idsToCheck = new HashSet<>();
        idsToCheck.add(tag.gettag_id());
        idsToCheck.add(tag2.gettag_id());
        HashSet<Guid> idsFromTagsDirector = tagsDirector.getTagIdAndChildrenIdsAsSet(tag.gettag_id());
        assertEquals(idsToCheck, idsFromTagsDirector);
    }

    @Test
    public void testGetTagIdAndChildrenIdsByName() {
        Tags tag = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        tag.getChildren().add(tag2);
        tag2.setparent_id(tag.getparent_id());
        tagsDirector.addTag(tag);
        String idsStr = tagsDirector.getTagIdAndChildrenIds(tag.gettag_name());
        String[] ids = idsStr.split("[,]");
        assertEquals(2, ids.length);
        assertEquals(ids[0], "'" + tag.gettag_id().toString() + "'");
        assertEquals(ids[1], "'" + tag2.gettag_id().toString() + "'");
    }

    /**
     * Test to check that bz https://bugzilla.redhat.com/722203 got solved. The test is a java translation of an
     * automation test that takes 4 tags, adds them to the tags director, changes one of the tags parent to be the other
     * tag, updates the tag, and queries the tags.
     */
    @Test
    public void testUpdate() {
        Tags tag = createTag("tag1", "desc1");
        tagsDirector.addTag(tag);
        tag.settag_name("new name");
        tagsDirector.updateTag(tag);
        Tags fromDirector = tagsDirector.getTagById(tag.gettag_id());
        assertEquals(tag.gettag_name(), fromDirector.gettag_name());
    }

    @Test
    public void testUpdateParent() {
        Tags tag1 = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        Tags tag3 = createTag("tag3", "desc3");
        Tags tag4 = createTag("tag4", "desc4");

        tagsDirector.addTag(tag1);
        tagsDirector.addTag(tag2);
        tagsDirector.addTag(tag3);
        tagsDirector.addTag(tag4);
        // Emulates the REST API behavior of getting all the tags prior to updating
        ArrayList<Tags> tags = tagsDirector.getAllTags();
        Tags tagToChange = null;
        for (Tags tag : tags) {
            if (tag.gettag_name().equals("tag1")) {
                tagToChange = tag;
                break;
            }
        }
        tagToChange.setparent_id(tag2.gettag_id());
        tagsDirector.updateTag(tagToChange);
        // Emulates the REST API behavior of getting all the tags after updating
        tags = tagsDirector.getAllTags();
        Tags changedTag = null;
        for (Tags tag : tags) {
            if (tag.gettag_name().equals("tag1")) {
                changedTag = tag;
                break;
            }
        }
        assertEquals(tag2.gettag_id(), changedTag.getparent_id());
    }

    public void testDirectDescetor() {
        Tags tag1 = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        tagsDirector.addTag(tag1);
        tagsDirector.addTag(tag2);
        tagsDirector.moveTag(tag2.gettag_id(), tag1.gettag_id());
        assertTrue(tagsDirector.isTagDescestorOfTag(tag1.gettag_id(), tag2.gettag_id()));
    }

    @Test
    public void testIsNonDirectDescetor() {
        Tags tag1 = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        Tags tag3 = createTag("tag3", "desc3");
        tagsDirector.addTag(tag1);
        tagsDirector.addTag(tag2);
        tagsDirector.addTag(tag3);
        tagsDirector.moveTag(tag3.gettag_id(), tag1.gettag_id());
        tagsDirector.moveTag(tag2.gettag_id(), tag3.gettag_id());
        assertTrue(tagsDirector.isTagDescestorOfTag(tag1.gettag_id(), tag2.gettag_id()));
    }

    @Test
    public void testNotDescetor() {
        Tags tag1 = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        assertFalse(tagsDirector.isTagDescestorOfTag(tag1.gettag_id(), tag2.gettag_id()));
    }
}
