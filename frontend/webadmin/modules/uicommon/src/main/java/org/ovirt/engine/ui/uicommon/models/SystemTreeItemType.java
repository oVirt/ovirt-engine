package org.ovirt.engine.ui.uicommon.models;
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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;

@SuppressWarnings("unused")
public enum SystemTreeItemType
{
	System,
	DataCenter,
	Storages,
	Storage,
	Templates,
	Clusters,
	Cluster,
	VMs,
	Hosts,
	Host;

	public int getValue()
	{
		return this.ordinal();
	}

	public static SystemTreeItemType forValue(int value)
	{
		return values()[value];
	}
}