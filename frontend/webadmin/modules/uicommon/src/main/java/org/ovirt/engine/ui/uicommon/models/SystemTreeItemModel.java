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
public class SystemTreeItemModel extends EntityModel
{
	private SystemTreeItemType type = SystemTreeItemType.values()[0];
	public SystemTreeItemType getType()
	{
		return type;
	}
	public void setType(SystemTreeItemType value)
	{
		if (type != value)
		{
			type = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Type"));
		}
	}

	private java.util.List<SystemTreeItemModel> privateChildren;
	public java.util.List<SystemTreeItemModel> getChildren()
	{
		return privateChildren;
	}
	public void setChildren(java.util.List<SystemTreeItemModel> value)
	{
		privateChildren = value;
	}

	private SystemTreeItemModel privateParent;
	public SystemTreeItemModel getParent()
	{
		return privateParent;
	}
	public void setParent(SystemTreeItemModel value)
	{
		privateParent = value;
	}

	private boolean isExpanded;
	public boolean getIsExpanded()
	{
		return isExpanded;
	}
	public void setIsExpanded(boolean value)
	{
		if (isExpanded != value)
		{
			isExpanded = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsExpanded"));
		}
	}


	public SystemTreeItemModel()
	{
		setChildren(new ObservableCollection<SystemTreeItemModel>());
	}


	public static SystemTreeItemModel FindAncestor(SystemTreeItemType type, SystemTreeItemModel root)
	{
		if (root.getType() != type)
		{
			if (root.getParent() != null)
			{
				return FindAncestor(type, root.getParent());
			}

			return null;
		}

		return root;
	}
}