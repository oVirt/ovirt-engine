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
public class SpiceMenuContainerItem extends SpiceMenuItem
{
	private String privateText;
	public String getText()
	{
		return privateText;
	}
	public void setText(String value)
	{
		privateText = value;
	}

	private java.util.List<SpiceMenuItem> items;
	public java.util.List<SpiceMenuItem> getItems()
	{
		if (items == null)
		{
			items = new java.util.ArrayList<SpiceMenuItem>();
		}

		return items;
	}

	public SpiceMenuContainerItem(int id, String text)
	{
		setId(id);
		setText(text);
	}
}