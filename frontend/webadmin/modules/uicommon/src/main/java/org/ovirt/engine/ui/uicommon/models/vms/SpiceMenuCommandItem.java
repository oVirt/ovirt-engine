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
public class SpiceMenuCommandItem extends SpiceMenuItem
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
	private String privateCommandName;
	public String getCommandName()
	{
		return privateCommandName;
	}
	public void setCommandName(String value)
	{
		privateCommandName = value;
	}


	public SpiceMenuCommandItem(int id, String text, String commandName)
	{
		setId(id);
		setText(text);
		setCommandName(commandName);
	}
}