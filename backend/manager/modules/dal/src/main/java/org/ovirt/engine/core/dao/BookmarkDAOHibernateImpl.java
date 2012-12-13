package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>BookmarkDAOHibernateImpl</code> provides an implementation of {@link BookmarkDAO} that uses Hibernate for
 * persistence.
 */
public class BookmarkDAOHibernateImpl extends BaseDAOHibernateImpl<Bookmark, Guid> implements BookmarkDAO {
    public BookmarkDAOHibernateImpl() {
        super(Bookmark.class);
    }
}
