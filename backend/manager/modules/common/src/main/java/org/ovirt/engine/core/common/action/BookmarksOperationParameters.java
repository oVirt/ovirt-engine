package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.validation.Valid;

public class BookmarksOperationParameters extends BookmarksParametersBase {
    private static final long serialVersionUID = 904048653429089175L;
    @Valid
    private bookmarks _bookmark;

    public BookmarksOperationParameters(bookmarks bookmark) {
        super(bookmark.getbookmark_id());
        _bookmark = bookmark;
    }

    public bookmarks getBookmark() {
        return _bookmark;
    }

    public BookmarksOperationParameters() {
    }
}
