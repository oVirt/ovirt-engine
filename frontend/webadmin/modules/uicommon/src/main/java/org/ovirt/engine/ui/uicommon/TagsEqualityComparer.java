package org.ovirt.engine.ui.uicommon;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.core.common.businessentities.*;

@SuppressWarnings("unused")
public class TagsEqualityComparer implements IEqualityComparer<tags>
{
	public boolean equals(tags x, tags y)
	{
		return x.gettag_id().equals(y.gettag_id());
	}

	public int hashCode(tags tag)
	{
		return tag.gettag_id().hashCode();
	}
}