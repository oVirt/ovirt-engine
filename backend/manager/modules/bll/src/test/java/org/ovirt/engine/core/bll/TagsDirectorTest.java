package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.TransformerUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.TagDAO;

public class TagsDirectorTest {

    private TagsDirector tagsDirector;

    @Mock
    private TagDAO tagDao;

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
        MockitoAnnotations.initMocks(this);
        tagsDirector = spy(TagsDirector.getInstance());
        when(tagDao.getAllForParent(any(Guid.class))).thenReturn((List<tags>) Collections.EMPTY_LIST);
        doReturn(tagDao).when(tagsDirector).getTagDAO();
        doNothing().when(tagsDirector).updateTagInBackend(any(tags.class));
        tagsDirector.init();
    }

    protected tags createTag(String name, String description) {
        Guid tagId = Guid.NewGuid();
        tags tag = new tags();
        tag.setdescription(description);
        tag.settag_id(tagId);
        tag.settag_name(name);
        tag.settype(TagsType.GeneralTag);
        tag.setparent_id(tagsDirector.GetRootTag().gettag_id());
        return tag;
    }

    @Test
    public void testCloneTag() {
        tags tag = createTag("a", "b");
        tags tag2 = createTag("c", "d");
        tag.getChildren().add(tag2);
        tag2.setparent_id(tag.gettag_id());
        Transformer cloner = TransformerUtils.cloneTransformer();
        tags newTag = (tags) cloner.transform(tag);
        assertEquals(1, newTag.getChildren().size());
    }

    @Test
    public void testAddTag() {
        tags tag = createTag("tag1", "desc1");

        tags tagFromDirector = tagsDirector.GetTagById(tag.gettag_id());
        assertNull(tagFromDirector);

        tagsDirector.AddTag(tag);
        tagFromDirector = tagsDirector.GetTagById(tag.gettag_id());
        assertNotNull(tagFromDirector);
        assertEquals(tag, tagFromDirector);
    }

    @Test
    public void testChangeValueAfterAdd() {
        tags tag = createTag("tag1", "desc1");
        tagsDirector.AddTag(tag);
        tag.settag_name("tag2");
        tags tagFromDirector = tagsDirector.GetTagById(tag.gettag_id());
        assertNotNull(tagFromDirector);
        assertEquals("tag1", tagFromDirector.gettag_name());
    }

    @Test
    public void testGetRootTag() {
        tags tags = tagsDirector.GetRootTag();
        assertNotNull(tags);
    }

    @Test
    public void testGetRootWithHierarchy() {
        tags tag = createTag("tag1", "desc1");
        tagsDirector.AddTag(tag);
        tags tags = tagsDirector.GetRootTag();
        assertNotNull(tags);
        assertEquals(tags.gettag_id(), TagsDirector.ROOT_TAG_ID);
        assertEquals(1, tags.getChildren().size());
        assertEquals("tag1", tags.getChildren().get(0).gettag_name());
    }

    @Test
    public void testUpdateChildren() {
        tags tag = createTag("tag1", "desc1");
        tagsDirector.AddTag(tag);
        tag.settag_name("booboo");
        tagsDirector.UpdateTag(tag);
        tags rootTag = tagsDirector.GetRootTag();
        tag = rootTag.getChildren().get(0);
        assertEquals("booboo", tag.gettag_name());
    }

    @Test
    public void testMoveTag() {
        //let's have two top level tag under root
        tags level1obj1 = createTag("level1obj1", "");
        level1obj1.settag_id(Guid.NewGuid());
        level1obj1.setparent_id(tagsDirector.GetRootTag().gettag_id());
        tagsDirector.AddTag(level1obj1);
        tags level1obj2 = createTag("level1obj2", "");
        level1obj2.settag_id(Guid.NewGuid());
        level1obj2.setparent_id(tagsDirector.GetRootTag().gettag_id());
        tagsDirector.AddTag(level1obj2);

        //now none of these should have any children
        Assert.assertEquals(0, tagsDirector.GetTagById(level1obj1.gettag_id()).getChildren().size());
        Assert.assertEquals(0, tagsDirector.GetTagById(level1obj2.gettag_id()).getChildren().size());

        //now let's add a child tag o the first top level tag
        tags level2obj1 = createTag("level2obj1", "");
        level2obj1.settag_id(Guid.NewGuid());
        level2obj1.setparent_id(level1obj1.gettag_id());
        tagsDirector.AddTag(level2obj1);

        //now check the number of children
        Assert.assertEquals(1, tagsDirector.GetTagById(level1obj1.gettag_id()).getChildren().size());
        Assert.assertEquals(0, tagsDirector.GetTagById(level1obj2.gettag_id()).getChildren().size());

        //should be all right so far.
        //now let's do the trick: move the second level tag to under the other first level tag
        tagsDirector.MoveTag(level2obj1.gettag_id(), level1obj2.gettag_id());

        //and now let's recheck, the first top level should have 0 children, the second should have 1
        Assert.assertEquals(0, tagsDirector.GetTagById(level1obj1.gettag_id()).getChildren().size());
        Assert.assertEquals(1, tagsDirector.GetTagById(level1obj2.gettag_id()).getChildren().size());

    }

    @Test
    public void testMoveTag_root() {
        //let's have two top level tag under root
        tags level1obj1 = createTag("level1obj1", "");
        level1obj1.settag_id(Guid.NewGuid());
        level1obj1.setparent_id(tagsDirector.GetRootTag().gettag_id());
        tagsDirector.AddTag(level1obj1);
        tags level1obj2 = createTag("level1obj2", "");
        level1obj2.settag_id(Guid.NewGuid());
        level1obj2.setparent_id(tagsDirector.GetRootTag().gettag_id());
        tagsDirector.AddTag(level1obj2);

        //now none of these should have any children
        Assert.assertEquals(0, tagsDirector.GetTagById(level1obj1.gettag_id()).getChildren().size());
        Assert.assertEquals(0, tagsDirector.GetTagById(level1obj2.gettag_id()).getChildren().size());
        Assert.assertEquals(2, tagsDirector.GetRootTag().getChildren().size());

        //should be all right so far.
        //now let's do the trick: move the second level tag to under the other first level tag
        tagsDirector.MoveTag(level1obj1.gettag_id(), level1obj2.gettag_id());

        //and now let's recheck, the first top level should have 0 children, the second should have 1
        Assert.assertEquals(1, tagsDirector.GetTagById(level1obj2.gettag_id()).getChildren().size());
        Assert.assertEquals(1, tagsDirector.GetRootTag().getChildren().size());

    }

    @Test
    public void testUpdateParentTag() {
        tags tag = createTag("tag1", "desc1");
        tag.settag_id(Guid.NewGuid());
        tagsDirector.AddTag(tag);
        tags rootTag = tagsDirector.GetRootTag();
        tag = rootTag.getChildren().get(0);
        assertEquals("tag1", tag.gettag_name());

        // now let's add another tag
        tags sub = createTag("subtag1", "subdesc");
        sub.settag_id(Guid.NewGuid());
        sub.setparent_id(tag.gettag_id());
        tagsDirector.AddTag(sub);

        //so now the root tag must have 1 child
        Assert.assertEquals(1, tagsDirector.GetRootTag().getChildren().size());
        Assert.assertEquals(1, tagsDirector.GetTagById(tag.gettag_id()).getChildren().size());

        // get the parent, and rename it
        tag.settag_name("subtag1_up");
        tagsDirector.UpdateTag(tag);

        // now let's see the number of children in the tag objects
        //this is the assertion that fails without fix for #732640
        Assert.assertEquals(1, tagsDirector.GetRootTag().getChildren().size());
        Assert.assertEquals(1, tagsDirector.GetTagById(tag.gettag_id()).getChildren().size());

        //let's check the same thing on overwriting description
        tag.setdescription("TEST TEST TEST TEST");
        tagsDirector.UpdateTag(tag);

        // and all the checks once again just to make sure
        Assert.assertEquals(1, tagsDirector.GetRootTag().getChildren().size());
        Assert.assertEquals(1, tagsDirector.GetTagById(tag.gettag_id()).getChildren().size());

    }

    @Test
    public void testGetTagByNameNotExists() {
        tags fromTagsDirector = tagsDirector.GetTagByName("does not exist");
        assertNull(fromTagsDirector);
    }

    @Test
    public void testGetByName() {
        tags tag = createTag("tag1", "desc1");
        tagsDirector.AddTag(tag);
        tags fromTagsDirector = tagsDirector.GetTagByName("tag1");
        assertNotNull(fromTagsDirector);
        assertEquals(tag, fromTagsDirector);
    }

    @Test
    public void testGetAllTags() {
        ArrayList<tags> allTags = tagsDirector.GetAllTags();
        assertEquals(0, allTags.size());
        tags tag = createTag("tag1", "desc1");
        tagsDirector.AddTag(tag);
        allTags = tagsDirector.GetAllTags();
        assertEquals(1, allTags.size());
        tag = createTag("tag2", "desc2");
        tagsDirector.AddTag(tag);
        allTags = tagsDirector.GetAllTags();
        assertEquals(2, allTags.size());
    }

    @Test
    public void testGetTagIdAndChildrenIdsNotExists() {
        tags tag = createTag("tag1", "desc1");
        String idsStr = tagsDirector.GetTagIdAndChildrenIds(tag.gettag_id());
        assertEquals(StringUtils.EMPTY, idsStr);
    }

    @Test
    public void testGetTagIdAndChildrenIds() {
        tags tag = createTag("tag1", "desc1");
        tags tag2 = createTag("tag2", "desc2");
        tag.getChildren().add(tag2);
        tag2.setparent_id(tag.getparent_id());
        tagsDirector.AddTag(tag);
        String idsStr = tagsDirector.GetTagIdAndChildrenIds(tag.gettag_id());
        String[] ids = idsStr.split("[,]");
        assertEquals(2, ids.length);
        assertEquals(ids[0], "'" + tag.gettag_id().toString() + "'");
        assertEquals(ids[1], "'" + tag2.gettag_id().toString() + "'");
    }

    @Test
    public void testGetTagIdAndChildrenIdsAsSet() {
        tags tag = createTag("tag1", "desc1");
        tags tag2 = createTag("tag2", "desc2");
        tag.getChildren().add(tag2);
        tag2.setparent_id(tag.getparent_id());
        tagsDirector.AddTag(tag);
        Set<Guid> idsToCheck = new HashSet<Guid>();
        idsToCheck.add(tag.gettag_id());
        idsToCheck.add(tag2.gettag_id());
        HashSet<Guid> idsFromTagsDirector = tagsDirector.GetTagIdAndChildrenIdsAsSet(tag.gettag_id());
        assertEquals(idsToCheck, idsFromTagsDirector);
    }

    @Test
    public void testGetTagIdAndChildrenIdsByName() {
        tags tag = createTag("tag1", "desc1");
        tags tag2 = createTag("tag2", "desc2");
        tag.getChildren().add(tag2);
        tag2.setparent_id(tag.getparent_id());
        tagsDirector.AddTag(tag);
        String idsStr = tagsDirector.GetTagIdAndChildrenIds(tag.gettag_name());
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
        tags tag = createTag("tag1", "desc1");
        tagsDirector.AddTag(tag);
        tag.settag_name("new name");
        tagsDirector.UpdateTag(tag);
        tags fromDirector = tagsDirector.GetTagById(tag.gettag_id());
        assertEquals(tag.gettag_name(), fromDirector.gettag_name());
    }

    @Test
    public void testUpdateParent() {
        tags tag1 = createTag("tag1", "desc1");
        tags tag2 = createTag("tag2", "desc2");
        tags tag3 = createTag("tag3", "desc3");
        tags tag4 = createTag("tag4", "desc4");

        tagsDirector.AddTag(tag1);
        tagsDirector.AddTag(tag2);
        tagsDirector.AddTag(tag3);
        tagsDirector.AddTag(tag4);
        // Emulates the REST API behavior of getting all the tags prior to updating
        ArrayList<tags> tags = tagsDirector.GetAllTags();
        tags tagToChange = null;
        for (tags tag : tags) {
            if (tag.gettag_name().equals("tag1")) {
                tagToChange = tag;
                break;
            }
        }
        tagToChange.setparent_id(tag2.gettag_id());
        tagsDirector.UpdateTag(tagToChange);
        // Emulates the REST API behavior of getting all the tags after updating
        tags = tagsDirector.GetAllTags();
        tags changedTag = null;
        for (tags tag : tags) {
            if (tag.gettag_name().equals("tag1")) {
                changedTag = tag;
                break;
            }
        }
        assertEquals(tag2.gettag_id(), changedTag.getparent_id());
    }

    @Test
    public void testGetTagIdsAndChildrenIdsByRegExpNoTagMatches() {
        String result = tagsDirector.GetTagIdsAndChildrenIdsByRegExp("tag*");
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void testGetTagIdsAndChildrenIdsByRegExp() {
        tags tag1 = createTag("tag1", "desc1");
        tagsDirector.AddTag(tag1);
        validateRegexpQueryResult("tag1", tag1);
    }

    @Test
    public void testGetTagIdsAndChildrenIdsByRegExpWithWildcard() {
        tags tag1 = createTag("tag1", "desc1");
        tagsDirector.AddTag(tag1);
        validateRegexpQueryResult("tag1*", tag1);
    }

    @Test
    public void testGetTagIdsAndChildrenIdsByRegExpWithWildcardAndSeveralChildren() {
        tags tag1 = createTag("tag1", "desc1");
        tagsDirector.AddTag(tag1);
        tags tag2 = createTag("tag2", "desc2");
        tagsDirector.AddTag(tag2);
        validateRegexpQueryResult("tag*", tag1, tag2);
    }

    @Test
    public void testGetTagIdsAndChildrenIdsByRegExpWithWildcardAndSeveralChildrenAndHierachy() {
        tags tag1 = createTag("tag1", "desc1");
        tags tag2 = createTag("tag2", "desc2");
        tag2.getChildren().add(tag1);
        tag1.setparent_id(tag2.gettag_id());
        tagsDirector.AddTag(tag2);
        tagsDirector.AddTag(tag1);
        validateRegexpQueryResult("tag*", tag1, tag2);
    }

    private void validateRegexpQueryResult(String regexp, tags... tagArray) {
        String result = tagsDirector.GetTagIdsAndChildrenIdsByRegExp(regexp);
        for (int counter = 0; counter < tagArray.length; counter++) {
            tags tagToCheck = tagArray[counter];
            assertTrue(result.indexOf(tagToCheck.gettag_id().toString()) > -1);
        }
    }

    public void testDirectDescetor() {
        tags tag1 = createTag("tag1", "desc1");
        tags tag2 = createTag("tag2", "desc2");
        tagsDirector.AddTag(tag1);
        tagsDirector.AddTag(tag2);
        tagsDirector.MoveTag(tag2.gettag_id(), tag1.gettag_id());
        assertTrue(tagsDirector.IsTagDescestorOfTag(tag1.gettag_id(), tag2.gettag_id()));
    }

    @Test
    public void testIsNonDirectDescetor() {
        tags tag1 = createTag("tag1", "desc1");
        tags tag2 = createTag("tag2", "desc2");
        tags tag3 = createTag("tag3", "desc3");
        tagsDirector.AddTag(tag1);
        tagsDirector.AddTag(tag2);
        tagsDirector.AddTag(tag3);
        tagsDirector.MoveTag(tag3.gettag_id(), tag1.gettag_id());
        tagsDirector.MoveTag(tag2.gettag_id(), tag3.gettag_id());
        assertTrue(tagsDirector.IsTagDescestorOfTag(tag1.gettag_id(), tag2.gettag_id()));
    }

    @Test
    public void testNotDescetor() {
        tags tag1 = createTag("tag1", "desc1");
        tags tag2 = createTag("tag2", "desc2");
        assertFalse(tagsDirector.IsTagDescestorOfTag(tag1.gettag_id(), tag2.gettag_id()));
    }
}
