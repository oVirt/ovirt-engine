package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code BookmarkDaoImpl} provides a concrete implementation of {@link BookmarkDao}.
 */
@Named
@Singleton
public class BookmarkDaoImpl extends BaseDao implements BookmarkDao {
    private static final RowMapper<Bookmark> bookmarkRowMapper = (rs, rowNum) -> {
        Bookmark entity = new Bookmark();
        entity.setId(getGuid(rs, "bookmark_id"));
        entity.setName(rs.getString("bookmark_name"));
        entity.setValue(rs.getString("bookmark_value"));
        return entity;
    };

    private MapSqlParameterSource getBookmarkParameterSource(Bookmark bookmark) {
        return getCustomMapSqlParameterSource()
                .addValue("bookmark_id", bookmark.getId())
                .addValue("bookmark_name", bookmark.getName())
                .addValue("bookmark_value", bookmark.getValue());
    }

    @Override
    public Bookmark get(Guid id) {
        return getCallsHandler().executeRead("GetBookmarkBybookmark_id",
                bookmarkRowMapper,
                getCustomMapSqlParameterSource().addValue("bookmark_id", id));
    }

    @Override
    public Bookmark getByName(String name) {
        return getCallsHandler().executeRead("GetBookmarkBybookmark_name",
                bookmarkRowMapper,
                getCustomMapSqlParameterSource().addValue("bookmark_name", name));
    }

    @Override
    public List<Bookmark> getAll() {
        return getCallsHandler().executeReadList("GetAllFromBookmarks",
                bookmarkRowMapper,
                getCustomMapSqlParameterSource());
    }

    @Override
    public void save(Bookmark bookmark) {
        Guid id = bookmark.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            bookmark.setId(id);
        }
        getCallsHandler().executeModification("InsertBookmark", getBookmarkParameterSource(bookmark));
    }

    @Override
    public void update(Bookmark bookmark) {
        getCallsHandler().executeModification("UpdateBookmark", getBookmarkParameterSource(bookmark));
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteBookmark",
                getCustomMapSqlParameterSource().addValue("bookmark_id", id));
    }
}
