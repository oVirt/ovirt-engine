package org.ovirt.engine.ui.uicommonweb.models.bookmarks;

import org.ovirt.engine.core.compat.EventArgs;

@SuppressWarnings("unused")
public final class BookmarkEventArgs extends EventArgs
{
    private org.ovirt.engine.core.common.businessentities.bookmarks privateBookmark;

    public org.ovirt.engine.core.common.businessentities.bookmarks getBookmark()
    {
        return privateBookmark;
    }

    private void setBookmark(org.ovirt.engine.core.common.businessentities.bookmarks value)
    {
        privateBookmark = value;
    }

    public BookmarkEventArgs(org.ovirt.engine.core.common.businessentities.bookmarks bookmark)
    {
        setBookmark(bookmark);
    }
}
