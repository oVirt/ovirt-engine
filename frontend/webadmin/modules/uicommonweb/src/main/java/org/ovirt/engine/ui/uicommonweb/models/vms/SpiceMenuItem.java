package org.ovirt.engine.ui.uicommonweb.models.vms;
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
public abstract class SpiceMenuItem
{
	private int privateId;
	public int getId()
	{
		return privateId;
	}
	public void setId(int value)
	{
		privateId = value;
	}
	private boolean privateIsEnabled;
	public boolean getIsEnabled()
	{
		return privateIsEnabled;
	}
	public void setIsEnabled(boolean value)
	{
		privateIsEnabled = value;
	}

	protected SpiceMenuItem()
	{
		setIsEnabled(true);
	}
}