package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>BookmarkDAOTest</code> performs tests against the {@link BookmarkDAO} type.
 *
 *
 */
public class BookmarkDAOTest extends BaseDAOTestCase {
    private static final int BOOKMARK_COUNT = 2;
    private static final int BOOKMARK_MAX_RANDOM_NUMBER = 10000;

    private BookmarkDAO dao;
    private Bookmark new_bookmark;
    private Bookmark existing_bookmark;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getBookmarkDao();

        // create some test data
        new_bookmark = new Bookmark();
        Random r = new Random(System.currentTimeMillis());
        new_bookmark.setbookmark_name("newbookmarkname"+ (r.nextInt() % BOOKMARK_MAX_RANDOM_NUMBER));
        new_bookmark.setbookmark_value("newbookmarkvalue");

        existing_bookmark = dao.get(new Guid("a4affabf-7b45-4a6c-b0a9-107d0bbe265e"));
    }

    /**
     * Ensures that if the id is invalid then no bookmark is returned.
     */
    @Test
    public void testGetWithInvalidId() {
        Bookmark result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that, if the id is valid, then retrieving a bookmark works as expected.
     */
    @Test
    public void testGet() {
        Bookmark result = dao.get(existing_bookmark.getbookmark_id());

        assertNotNull(result);
        assertEquals(existing_bookmark.getbookmark_id(),
                result.getbookmark_id());
    }

    /**
     * Ensures that, if the supplied name is invalid, then no bookmark is returned.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        Bookmark result = dao.getByName("thisnameisinvalid");

        assertNull(result);
    }

    /**
     * Ensures that finding by name works as expected.
     */
    @Test
    public void testGetByName() {
        Bookmark result = dao.getByName(existing_bookmark.getbookmark_name());

        assertNotNull(result);
        assertEquals(existing_bookmark.getbookmark_name(),
                result.getbookmark_name());
    }

    /**
     * Ensures that finding all bookmarks works as expected.
     */
    @Test
    public void testGetAll() {
        List<Bookmark> result = dao.getAll();

        assertEquals(BOOKMARK_COUNT, result.size());
    }

    /**
     * Ensures that saving a bookmark works as expected.
     */
    @Test
    public void testSave() {
        dao.save(new_bookmark);

        Bookmark result = dao.getByName(new_bookmark.getbookmark_name());

        assertNotNull(result);
    }

    /**
     * Ensures that updating a bookmark works as expected.
     */
    @Test
    public void testUpdate() {
        existing_bookmark.setbookmark_name(existing_bookmark.getbookmark_name()
                .toUpperCase());

        dao.update(existing_bookmark);

        Bookmark result = dao.get(existing_bookmark.getbookmark_id());

        assertNotNull(result);
        assertEquals(existing_bookmark.getbookmark_name(),
                result.getbookmark_name());
    }

    /**
     * Ensures that removing a bookmark works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existing_bookmark.getbookmark_id());

        Bookmark result = dao.get(existing_bookmark.getbookmark_id());

        assertNull(result);
    }

    /**
     * Make sure that we work with detached entities in our code
     */
    @Test
    public void testDetachedEntityGet() {
        Bookmark result = dao.get(existing_bookmark.getbookmark_id());
        result.setbookmark_name("a test");
        Bookmark result2 = dao.get(existing_bookmark.getbookmark_id());
        assertNotEquals(result.getbookmark_name(), result2.getbookmark_name());
    }
}
