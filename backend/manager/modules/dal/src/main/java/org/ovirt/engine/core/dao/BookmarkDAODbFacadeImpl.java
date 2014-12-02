package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>BookmarkDAODbFacadeImpl</code> provides a concrete implementation of <code>BookmarkDAO</code> that uses
 * pre-existing code from <code>DbFacade</code>
 *
 *
 */
@Named
public class BookmarkDAODbFacadeImpl extends BaseDAODbFacade implements BookmarkDAO {
    private static class BookmarkRowMapper implements RowMapper<Bookmark> {
        public static final BookmarkRowMapper instance = new BookmarkRowMapper();

        @Override
        public Bookmark mapRow(ResultSet rs, int rowNum) throws SQLException {
            Bookmark entity = new Bookmark();
            entity.setbookmark_id(getGuid(rs, "bookmark_id"));
            entity.setbookmark_name(rs.getString("bookmark_name"));
            entity.setbookmark_value(rs.getString("bookmark_value"));
            return entity;
        }
    }

    private class BookmarkSqlParameterSource extends
            CustomMapSqlParameterSource {
        public BookmarkSqlParameterSource(Bookmark bookmark) {
            super(dialect);
            addValue("bookmark_id", bookmark.getbookmark_id());
            addValue("bookmark_name", bookmark.getbookmark_name());
            addValue("bookmark_value", bookmark.getbookmark_value());
        }

        public BookmarkSqlParameterSource() {
            super(dialect);
        }

        public BookmarkSqlParameterSource(Guid id) {
            super(dialect);
            addValue("bookmark_id", id);
        }

        public BookmarkSqlParameterSource(String name) {
            super(dialect);
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
        Guid id = bookmark.getbookmark_id();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            bookmark.setbookmark_id(id);
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
