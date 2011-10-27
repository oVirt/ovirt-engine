package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.businessentities.bookmarks;

public abstract class BookmarkOperationCommand<T extends BookmarksOperationParameters> extends BookmarkCommandBase<T> {
    public BookmarkOperationCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected bookmarks getBookmark() {
        return getParameters().getBookmark();
    }

}
