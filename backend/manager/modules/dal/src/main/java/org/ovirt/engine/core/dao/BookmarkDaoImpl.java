package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>BookmarkDaoImpl</code> provides a concrete implementation of <code>BookmarkDao</code> that uses
 * pre-existing code from <code>DbFacade</code>
 */
@Named
@Singleton
public class BookmarkDaoImpl extends BaseDao implements BookmarkDao {
    private static class BookmarkRowMapper implements RowMapper<Bookmark> {
        public static final BookmarkRowMapper instance = new BookmarkRowMapper();

        @Override
        public Bookmark mapRow(ResultSet rs, int rowNum) throws SQLException {
            Bookmark entity = new Bookmark();
            entity.setId(getGuid(rs, "bookmark_id"));
            entity.setName(rs.getString("bookmark_name"));
            entity.setValue(rs.getString("bookmark_value"));
            return entity;
        }
    }

    private class BookmarkSqlParameterSource extends
            CustomMapSqlParameterSource {
        public BookmarkSqlParameterSource(Bookmark bookmark) {
            super(getDialect());
            addValue("bookmark_id", bookmark.getId());
            addValue("bookmark_name", bookmark.getName());
            addValue("bookmark_value", bookmark.getValue());
        }

        public BookmarkSqlParameterSource() {
            super(getDialect());
        }

        public BookmarkSqlParameterSource(Guid id) {
            super(getDialect());
            addValue("bookmark_id", id);
        }

        public BookmarkSqlParameterSource(String name) {
            super(getDialect());
            addValue("bookmark_name", name);
        }
    }

    @Override
    public Bookmark get(Guid id) {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource(
                id);
        return getCallsHandler().executeRead("GetBookmarkBybookmark_id", BookmarkRowMapper.instance, parameterSource);
    }

    @Override
    public Bookmark getByName(String name) {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource(
                name);
        return getCallsHandler().executeRead("GetBookmarkBybookmark_name", BookmarkRowMapper.instance, parameterSource);
    }

    @Override
    public List<Bookmark> getAll() {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromBookmarks", BookmarkRowMapper.instance, parameterSource);
    }

    @Override
    public void save(Bookmark bookmark) {
        Guid id = bookmark.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            bookmark.setId(id);
        }
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource(
                bookmark);

        getCallsHandler().executeModification("InsertBookmark", parameterSource);
    }

    @Override
    public void update(Bookmark bookmark) {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource(
                bookmark);

        getCallsHandler().executeModification("UpdateBookmark", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource(
                id);

        getCallsHandler().executeModification("DeleteBookmark", parameterSource);
    }
}
