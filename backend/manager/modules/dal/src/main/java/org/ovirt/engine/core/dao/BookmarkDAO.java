package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>BookmarkDAO</code> defines a type for performing CRUD operations on instances of <code>bookmarks</code>.
 *
 *
 */
public interface BookmarkDAO extends DAO {
    /**
     * Retrieves the specified bookmark with the supplied id.
     *
     * @param id
     * @return the bookmark
     */
    bookmarks get(Guid id);

    /**
     * Retrieves the bookmark with the supplied name.
     *
     * @param name
     *            the bookmark name
     * @return the bookmark, or <code>null</code> if no such bookmark is present
     */
    bookmarks getByName(String name);

    /**
     * Returns all defined bookmarks.
     *
     * @return the collection of bookmarks
     */
    List<bookmarks> getAll();

    /**
     * Saves the bookmark.
     *
     * @param bookmark
     */
    void save(bookmarks bookmark);

    /**
     * Updates the bookmark with changes.
     *
     * @param bookmark
     */
    void update(bookmarks bookmark);

    /**
     * Removes the specified bookmark.
     *
     * @param id
     *            the bookmark id
     */
    void remove(Guid id);
}
