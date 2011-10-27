package org.ovirt.engine.ui.uicommonweb.models.bookmarks;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

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