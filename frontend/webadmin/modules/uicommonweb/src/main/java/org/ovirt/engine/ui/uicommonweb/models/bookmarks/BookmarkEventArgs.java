package org.ovirt.engine.ui.uicommonweb.models.bookmarks;

import org.ovirt.engine.ui.uicompat.EventArgs;

@SuppressWarnings("unused")
public final class BookmarkEventArgs extends EventArgs {
    private org.ovirt.engine.core.common.businessentities.Bookmark privateBookmark;

    public org.ovirt.engine.core.common.businessentities.Bookmark getBookmark() {
        return privateBookmark;
    }

    private void setBookmark(org.ovirt.engine.core.common.businessentities.Bookmark value) {
        privateBookmark = value;
    }

    public BookmarkEventArgs(org.ovirt.engine.core.common.businessentities.Bookmark bookmark) {
        setBookmark(bookmark);
    }
}
