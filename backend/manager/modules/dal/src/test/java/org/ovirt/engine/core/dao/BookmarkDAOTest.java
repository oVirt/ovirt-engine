package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>BookmarkDAOTest</code> performs tests against the {@link BookmarkDAO} type.
 */
public class BookmarkDAOTest extends BaseHibernateDaoTestCase<BookmarkDAO, Bookmark, Guid> {
    private static final int BOOKMARK_COUNT = 2;
    private static final int BOOKMARK_MAX_RANDOM_NUMBER = 10000;

    private BookmarkDAO dao;
    private Bookmark newBookmark;
    private Bookmark existingBookmark;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getBookmarkDao();

        // create some test data
        newBookmark = new Bookmark();
        Random r = new Random(System.currentTimeMillis());
        newBookmark.setbookmark_id(Guid.newGuid());
        newBookmark.setbookmark_name("newbookmarkname" + (r.nextInt() % BOOKMARK_MAX_RANDOM_NUMBER));
        newBookmark.setbookmark_value("newbookmarkvalue");

        existingBookmark = dao.get(new Guid("a4affabf-7b45-4a6c-b0a9-107d0bbe265e"));
    }

    @Override
    protected BookmarkDAO getDao() {
        return dao;
    }

    @Override
    protected Bookmark getExistingEntity() {
        return existingBookmark;
    }

    @Override
    protected Bookmark getNonExistentEntity() {
        return newBookmark;
    }

    @Override
    protected int getAllEntitiesCount() {
        return BOOKMARK_COUNT;
    }

    @Override
    protected Bookmark modifyEntity(Bookmark bookmark) {
        bookmark.setbookmark_name(bookmark.getbookmark_name().toUpperCase());
        return bookmark;
    }

    @Override
    protected void verifyEntityModification(Bookmark result) {
        assertEquals(existingBookmark.getbookmark_name(), result.getbookmark_name());
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
        Bookmark result = dao.getByName(existingBookmark.getbookmark_name());

        assertNotNull(result);
        assertEquals(existingBookmark.getbookmark_name(), result.getbookmark_name());
    }

}
