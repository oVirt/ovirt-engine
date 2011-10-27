package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.bookmarks;
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
    private bookmarks new_bookmark;
    private bookmarks existing_bookmark;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getBookmarkDAO());

        // create some test data
        new_bookmark = new bookmarks();
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
        bookmarks result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that, if the id is valid, then retrieving a bookmark works as expected.
     */
    @Test
    public void testGet() {
        bookmarks result = dao.get(existing_bookmark.getbookmark_id());

        assertNotNull(result);
        assertEquals(existing_bookmark.getbookmark_id(),
                result.getbookmark_id());
    }

    /**
     * Ensures that, if the supplied name is invalid, then no bookmark is returned.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        bookmarks result = dao.getByName("thisnameisinvalid");

        assertNull(result);
    }

    /**
     * Ensures that finding by name works as expected.
     */
    @Test
    public void testGetByName() {
        bookmarks result = dao.getByName(existing_bookmark.getbookmark_name());

        assertNotNull(result);
        assertEquals(existing_bookmark.getbookmark_name(),
                result.getbookmark_name());
    }

    /**
     * Ensures that finding all bookmarks works as expected.
     */
    @Test
    public void testGetAll() {
        List<bookmarks> result = dao.getAll();

        assertEquals(BOOKMARK_COUNT, result.size());
    }

    /**
     * Ensures that saving a bookmark works as expected.
     */
    @Test
    public void testSave() {
        dao.save(new_bookmark);

        bookmarks result = dao.getByName(new_bookmark.getbookmark_name());

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

        bookmarks result = dao.get(existing_bookmark.getbookmark_id());

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

        bookmarks result = dao.get(existing_bookmark.getbookmark_id());

        assertNull(result);
    }
}
