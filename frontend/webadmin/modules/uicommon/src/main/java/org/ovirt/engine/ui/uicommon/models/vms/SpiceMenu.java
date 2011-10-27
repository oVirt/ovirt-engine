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
public class SpiceMenu
{
	private java.util.List<SpiceMenuItem> items;
	public java.util.List<SpiceMenuItem> getItems()
	{
		if (items == null)
		{
			items = new java.util.ArrayList<SpiceMenuItem>();
		}

		return items;
	}

	public java.util.List<SpiceMenuItem> Descendants()
	{
		java.util.ArrayList<SpiceMenuItem> list = new java.util.ArrayList<SpiceMenuItem>();
		for (SpiceMenuItem item : items)
		{
			DescendantsInternal(list, item);
		}

		return list;
	}

	private void DescendantsInternal(java.util.List<SpiceMenuItem> list, SpiceMenuItem root)
	{
		list.add(root);
		if (root instanceof SpiceMenuContainerItem)
		{
			for (SpiceMenuItem item : ((SpiceMenuContainerItem)root).getItems())
			{
				DescendantsInternal(list, item);
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (SpiceMenuItem item : getItems())
		{
			builder.append(ItemToString(item, null));
		}

		return builder.toString();
	}

	private String ItemToString(SpiceMenuItem item, SpiceMenuItem parent)
	{
		StringBuilder builder = new StringBuilder();
		int parentID = parent != null ? parent.getId() : 0;

		if (item instanceof SpiceMenuCommandItem)
		{
			SpiceMenuCommandItem commandItem = (SpiceMenuCommandItem)item;
			builder.append(StringFormat.format("%1$s\r%2$s\r%3$s\r%4$s\n", parentID, commandItem.getId(), commandItem.getText(), (commandItem.getIsEnabled()) ? 0 : 2));
		}

		if (item instanceof SpiceMenuContainerItem)
		{
			SpiceMenuContainerItem containerItem = (SpiceMenuContainerItem)item;
			builder.append(StringFormat.format("%1$s\r%2$s\r%3$s\r4\n", parentID, containerItem.getId(), containerItem.getText()));

			if (containerItem.getItems().size() > 0)
			{
				for (SpiceMenuItem localItem : containerItem.getItems())
				{
					builder.append(ItemToString(localItem, containerItem));
				}
			}
		}

		if (item instanceof SpiceMenuSeparatorItem)
		{
			builder.append(StringFormat.format("%1$s\r%2$s\r%3$s\r1\n", parentID, item.getId(), "-"));
		}


		return builder.toString();
	}
}