package org.ovirt.engine.core.dao;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.TypedQuery;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.jpa.AbstractJpaDao;

@Named
@Singleton
class BookmarkDaoImpl extends AbstractJpaDao<Bookmark, Guid> implements BookmarkDao {

    BookmarkDaoImpl() {
        super(Bookmark.class);
    }

    @Override
    public Bookmark getByName(String name) {
        final TypedQuery<Bookmark> query = entityManager.createNamedQuery("Bookmark.byName", Bookmark.class);
        query.setParameter("name", name);

        return singleResult(query);
    }

}
