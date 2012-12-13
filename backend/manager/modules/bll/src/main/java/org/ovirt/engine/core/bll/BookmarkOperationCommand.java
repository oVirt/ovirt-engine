package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.businessentities.Bookmark;

public abstract class BookmarkOperationCommand<T extends BookmarksOperationParameters> extends BookmarkCommandBase<T> {
    public BookmarkOperationCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected Bookmark getBookmark() {
        return getParameters().getBookmark();
    }

}
