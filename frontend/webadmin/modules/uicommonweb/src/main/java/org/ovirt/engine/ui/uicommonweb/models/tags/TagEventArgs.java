package org.ovirt.engine.ui.uicommonweb.models.tags;
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
public final class TagEventArgs extends EventArgs
{
	private TagModel privateTag;
	public TagModel getTag()
	{
		return privateTag;
	}
	private void setTag(TagModel value)
	{
		privateTag = value;
	}

	public TagEventArgs(TagModel tag)
	{
		setTag(tag);
	}
}