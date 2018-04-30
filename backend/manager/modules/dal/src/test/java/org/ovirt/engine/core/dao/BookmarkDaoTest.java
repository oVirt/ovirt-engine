package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code BookmarkDaoTest} performs tests against the {@link BookmarkDao} type.
 */
public class BookmarkDaoTest extends BaseDaoTestCase<BookmarkDao> {
    private static final int BOOKMARK_COUNT = 2;
    private static final int BOOKMARK_MAX_RANDOM_NUMBER = 10000;

    private Bookmark new_bookmark;
    private Bookmark existing_bookmark;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // create some test data
        new_bookmark = new Bookmark();
        Random r = new Random(System.currentTimeMillis());
        new_bookmark.setName("newbookmarkname" + (r.nextInt() % BOOKMARK_MAX_RANDOM_NUMBER));
        new_bookmark.setValue("newbookmarkvalue");

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
        Bookmark result = dao.get(existing_bookmark.getId());

        assertNotNull(result);
        assertEquals(existing_bookmark.getId(),
                result.getId());
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
        Bookmark result = dao.getByName(existing_bookmark.getName());

        assertNotNull(result);
        assertEquals(existing_bookmark.getName(),
                result.getName());
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

        Bookmark result = dao.getByName(new_bookmark.getName());

        assertNotNull(result);
    }

    /**
     * Ensures that updating a bookmark works as expected.
     */
    @Test
    public void testUpdate() {
        existing_bookmark.setName(existing_bookmark.getName()
                .toUpperCase());

        dao.update(existing_bookmark);

        Bookmark result = dao.get(existing_bookmark.getId());

        assertNotNull(result);
        assertEquals(existing_bookmark.getName(),
                result.getName());
    }

    /**
     * Ensures that removing a bookmark works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existing_bookmark.getId());

        Bookmark result = dao.get(existing_bookmark.getId());

        assertNull(result);
    }
}
