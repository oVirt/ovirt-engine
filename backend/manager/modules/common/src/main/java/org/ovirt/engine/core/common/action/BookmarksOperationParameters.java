package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.Bookmark;

public class BookmarksOperationParameters extends BookmarksParametersBase {
    private static final long serialVersionUID = 904048653429089175L;
    @Valid
    private Bookmark bookmark;

    public BookmarksOperationParameters(Bookmark bookmark) {
        super(bookmark.getId());
        this.bookmark = bookmark;
    }

    public Bookmark getBookmark() {
        return bookmark;
    }

    public BookmarksOperationParameters() {
    }
}
