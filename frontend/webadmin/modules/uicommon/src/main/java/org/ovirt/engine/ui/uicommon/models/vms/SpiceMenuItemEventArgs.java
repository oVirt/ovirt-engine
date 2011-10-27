package org.ovirt.engine.ui.uicommon.models.vms;
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

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public final class SpiceMenuItemEventArgs extends EventArgs
{
	private int privateMenuItemId;
	public int getMenuItemId()
	{
		return privateMenuItemId;
	}
	private void setMenuItemId(int value)
	{
		privateMenuItemId = value;
	}

	public SpiceMenuItemEventArgs(int menuItemId)
	{
		setMenuItemId(menuItemId);
	}
}