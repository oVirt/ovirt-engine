package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.TagDao;

@ExtendWith(MockitoExtension.class)
public class TagsDirectorTest {

    @InjectMocks
    @Spy
    private TagsDirector tagsDirector = new TagsDirector();

    @Mock
    private TagDao tagDao;

    @Mock
    private BackendInternal backendInternal;

    @BeforeEach
    public void setup() {
        tagsDirector.init();
    }

    protected Tags createTag(String name, String description) {
        Guid tagId = Guid.newGuid();
        Tags tag = new Tags();
        tag.setDescription(description);
        tag.setTagId(tagId);
        tag.setTagName(name);
        tag.setType(TagsType.GeneralTag);
        tag.setParentId(tagsDirector.getRootTag().getTagId());
        return tag;
    }

    @Test
    public void testAddTag() {
        Tags tag = createTag("tag1", "desc1");

        Tags tagFromDirector = tagsDirector.getTagById(tag.getTagId());
        assertNull(tagFromDirector);

        tagsDirector.addTag(tag);
        tagFromDirector = tagsDirector.getTagById(tag.getTagId());
        assertNotNull(tagFromDirector);
        assertEquals(tag, tagFromDirector);
    }

    @Test
    public void testChangeValueAfterAdd() {
        Tags tag = createTag("tag1", "desc1");
        tagsDirector.addTag(tag);
        tag.setTagName("tag2");
        Tags tagFromDirector = tagsDirector.getTagById(tag.getTagId());
        assertNotNull(tagFromDirector);
        assertEquals("tag1", tagFromDirector.getTagName());
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
        assertEquals(TagsDirector.ROOT_TAG_ID, tags.getTagId());
        assertEquals(1, tags.getChildren().size());
        assertEquals("tag1", tags.getChildren().get(0).getTagName());
    }

    @Test
    public void testUpdateChildren() {
        Tags tag = createTag("tag1", "desc1");
        tagsDirector.addTag(tag);
        tag.setTagName("booboo");
        tagsDirector.updateTag(tag);
        Tags rootTag = tagsDirector.getRootTag();
        tag = rootTag.getChildren().get(0);
        assertEquals("booboo", tag.getTagName());
    }

    @Test
    public void testMoveTag() {
        // let's have two top level tag under root
        Tags level1obj1 = createTag("level1obj1", "");
        level1obj1.setTagId(Guid.newGuid());
        level1obj1.setParentId(tagsDirector.getRootTag().getTagId());
        tagsDirector.addTag(level1obj1);
        Tags level1obj2 = createTag("level1obj2", "");
        level1obj2.setTagId(Guid.newGuid());
        level1obj2.setParentId(tagsDirector.getRootTag().getTagId());
        tagsDirector.addTag(level1obj2);

        // now none of these should have any children
        assertEquals(0, tagsDirector.getTagById(level1obj1.getTagId()).getChildren().size());
        assertEquals(0, tagsDirector.getTagById(level1obj2.getTagId()).getChildren().size());

        // now let's add a child tag o the first top level tag
        Tags level2obj1 = createTag("level2obj1", "");
        level2obj1.setTagId(Guid.newGuid());
        level2obj1.setParentId(level1obj1.getTagId());
        tagsDirector.addTag(level2obj1);

        // now check the number of children
        assertEquals(1, tagsDirector.getTagById(level1obj1.getTagId()).getChildren().size());
        assertEquals(0, tagsDirector.getTagById(level1obj2.getTagId()).getChildren().size());

        // should be all right so far.
        // now let's do the trick: move the second level tag to under the other first level tag
        tagsDirector.moveTag(level2obj1.getTagId(), level1obj2.getTagId());

        // and now let's recheck, the first top level should have 0 children, the second should have 1
        assertEquals(0, tagsDirector.getTagById(level1obj1.getTagId()).getChildren().size());
        assertEquals(1, tagsDirector.getTagById(level1obj2.getTagId()).getChildren().size());

    }

    @Test
    public void testMoveTagRoot() {
        // let's have two top level tag under root
        Tags level1obj1 = createTag("level1obj1", "");
        level1obj1.setTagId(Guid.newGuid());
        level1obj1.setParentId(tagsDirector.getRootTag().getTagId());
        tagsDirector.addTag(level1obj1);
        Tags level1obj2 = createTag("level1obj2", "");
        level1obj2.setTagId(Guid.newGuid());
        level1obj2.setParentId(tagsDirector.getRootTag().getTagId());
        tagsDirector.addTag(level1obj2);

        // now none of these should have any children
        assertEquals(0, tagsDirector.getTagById(level1obj1.getTagId()).getChildren().size());
        assertEquals(0, tagsDirector.getTagById(level1obj2.getTagId()).getChildren().size());
        assertEquals(2, tagsDirector.getRootTag().getChildren().size());

        // should be all right so far.
        // now let's do the trick: move the second level tag to under the other first level tag
        tagsDirector.moveTag(level1obj1.getTagId(), level1obj2.getTagId());

        // and now let's recheck, the first top level should have 0 children, the second should have 1
        assertEquals(1, tagsDirector.getTagById(level1obj2.getTagId()).getChildren().size());
        assertEquals(1, tagsDirector.getRootTag().getChildren().size());

    }

    @Test
    public void testUpdateParentTag() {
        Tags tag = createTag("tag1", "desc1");
        tag.setTagId(Guid.newGuid());
        tagsDirector.addTag(tag);
        Tags rootTag = tagsDirector.getRootTag();
        tag = rootTag.getChildren().get(0);
        assertEquals("tag1", tag.getTagName());

        // now let's add another tag
        Tags sub = createTag("subtag1", "subdesc");
        sub.setTagId(Guid.newGuid());
        sub.setParentId(tag.getTagId());
        tagsDirector.addTag(sub);

        // so now the root tag must have 1 child
        assertEquals(1, tagsDirector.getRootTag().getChildren().size());
        assertEquals(1, tagsDirector.getTagById(tag.getTagId()).getChildren().size());

        // get the parent, and rename it
        tag.setTagName("subtag1_up");
        tagsDirector.updateTag(tag);

        // now let's see the number of children in the tag objects
        // this is the assertion that fails without fix for #732640
        assertEquals(1, tagsDirector.getRootTag().getChildren().size());
        assertEquals(1, tagsDirector.getTagById(tag.getTagId()).getChildren().size());

        // let's check the same thing on overwriting description
        tag.setDescription("TEST TEST TEST TEST");
        tagsDirector.updateTag(tag);

        // and all the checks once again just to make sure
        assertEquals(1, tagsDirector.getRootTag().getChildren().size());
        assertEquals(1, tagsDirector.getTagById(tag.getTagId()).getChildren().size());

    }

    @Test
    public void testGetTagByNameNotExists() {
        Tags fromTagsDirector = tagsDirector.getTagByTagName("does not exist");
        assertNull(fromTagsDirector);
    }

    @Test
    public void testGetByName() {
        Tags tag = createTag("tag1", "desc1");
        tagsDirector.addTag(tag);
        Tags fromTagsDirector = tagsDirector.getTagByTagName("tag1");
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
        String idsStr = tagsDirector.getTagIdAndChildrenIds(tag.getTagId());
        assertEquals(StringUtils.EMPTY, idsStr);
    }

    @Test
    public void testGetTagIdAndChildrenIds() {
        Tags tag = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        tag.getChildren().add(tag2);
        tag2.setParentId(tag.getParentId());
        tagsDirector.addTag(tag);
        String idsStr = tagsDirector.getTagIdAndChildrenIds(tag.getTagId());
        String[] ids = idsStr.split("[,]");
        assertEquals(2, ids.length);
        assertEquals(ids[0], "'" + tag.getTagId().toString() + "'");
        assertEquals(ids[1], "'" + tag2.getTagId().toString() + "'");
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
        tag.setTagName("new name");
        tagsDirector.updateTag(tag);
        Tags fromDirector = tagsDirector.getTagById(tag.getTagId());
        assertEquals(tag.getTagName(), fromDirector.getTagName());
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
        Tags tagToChange = tags.stream().filter(t -> t.getTagName().equals("tag1")).findFirst().orElse(null);
        tagToChange.setParentId(tag2.getTagId());
        tagsDirector.updateTag(tagToChange);
        // Emulates the REST API behavior of getting all the tags after updating
        tags = tagsDirector.getAllTags();
        Tags changedTag = tags.stream().filter(t -> t.getTagName().equals("tag1")).findFirst().orElse(null);
        assertEquals(tag2.getTagId(), changedTag.getParentId());
    }

    @Test
    public void testDirectDescetor() {
        Tags tag1 = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        tagsDirector.addTag(tag1);
        tagsDirector.addTag(tag2);
        tagsDirector.moveTag(tag2.getTagId(), tag1.getTagId());
        assertTrue(tagsDirector.isTagDescestorOfTag(tag1.getTagId(), tag2.getTagId()));
    }

    @Test
    public void testIsNonDirectDescetor() {
        Tags tag1 = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        Tags tag3 = createTag("tag3", "desc3");
        tagsDirector.addTag(tag1);
        tagsDirector.addTag(tag2);
        tagsDirector.addTag(tag3);
        tagsDirector.moveTag(tag3.getTagId(), tag1.getTagId());
        tagsDirector.moveTag(tag2.getTagId(), tag3.getTagId());
        assertTrue(tagsDirector.isTagDescestorOfTag(tag1.getTagId(), tag2.getTagId()));
    }

    @Test
    public void testNotDescetor() {
        Tags tag1 = createTag("tag1", "desc1");
        Tags tag2 = createTag("tag2", "desc2");
        assertFalse(tagsDirector.isTagDescestorOfTag(tag1.getTagId(), tag2.getTagId()));
    }
}
