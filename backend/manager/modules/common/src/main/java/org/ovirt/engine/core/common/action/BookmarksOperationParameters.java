package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.validation.Valid;

public class BookmarksOperationParameters extends BookmarksParametersBase {
    private static final long serialVersionUID = 904048653429089175L;
    @Valid
    private Bookmark _bookmark;

    public BookmarksOperationParameters(Bookmark bookmark) {
        super(bookmark.getbookmark_id());
        _bookmark = bookmark;
    }

    public Bookmark getBookmark() {
        return _bookmark;
    }

    public BookmarksOperationParameters() {
    }
}
