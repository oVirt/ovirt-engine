package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class BookmarksParametersBase extends VdcActionParametersBase {
    private static final long serialVersionUID = 2719098594290719344L;
    private Guid _bookmarkId;

    public BookmarksParametersBase(Guid bookmarkId) {
        _bookmarkId = bookmarkId;
    }

    public Guid getBookmarkId() {
        return _bookmarkId;
    }

    public BookmarksParametersBase() {
    }
}
