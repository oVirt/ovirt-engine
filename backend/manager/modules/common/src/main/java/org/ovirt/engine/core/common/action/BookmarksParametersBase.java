package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class BookmarksParametersBase extends ActionParametersBase {
    private static final long serialVersionUID = 2719098594290719344L;
    private Guid bookmarkId;

    public BookmarksParametersBase(Guid bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public Guid getBookmarkId() {
        return bookmarkId;
    }

    public BookmarksParametersBase() {
    }
}
