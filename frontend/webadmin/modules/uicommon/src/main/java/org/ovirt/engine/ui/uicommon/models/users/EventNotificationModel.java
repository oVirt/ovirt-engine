package org.ovirt.engine.ui.uicommon.models.users;
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

import org.ovirt.engine.ui.uicommon.models.common.*;
import org.ovirt.engine.ui.uicommon.validation.*;

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class EventNotificationModel extends Model
{

	private UICommand privateExpandAllCommand;
	public UICommand getExpandAllCommand()
	{
		return privateExpandAllCommand;
	}
	private void setExpandAllCommand(UICommand value)
	{
		privateExpandAllCommand = value;
	}
	private UICommand privateCollapseAllCommand;
	public UICommand getCollapseAllCommand()
	{
		return privateCollapseAllCommand;
	}
	private void setCollapseAllCommand(UICommand value)
	{
		privateCollapseAllCommand = value;
	}


	private boolean privateIsNew;
	public boolean getIsNew()
	{
		return privateIsNew;
	}
	public void setIsNew(boolean value)
	{
		privateIsNew = value;
	}

	private EntityModel privateEmail;
	public EntityModel getEmail()
	{
		return privateEmail;
	}
	private void setEmail(EntityModel value)
	{
		privateEmail = value;
	}
	private String privateOldEmail;
	public String getOldEmail()
	{
		return privateOldEmail;
	}
	public void setOldEmail(String value)
	{
		privateOldEmail = value;
	}

	private java.util.ArrayList<SelectionTreeNodeModel> eventGroupModels;
	public java.util.ArrayList<SelectionTreeNodeModel> getEventGroupModels()
	{
		return eventGroupModels;
	}
	public void setEventGroupModels(java.util.ArrayList<SelectionTreeNodeModel> value)
	{
		if ((eventGroupModels == null && value != null) || (eventGroupModels != null && !eventGroupModels.equals(value)))
		{
			eventGroupModels = value;
			OnPropertyChanged(new PropertyChangedEventArgs("EventGroupModels"));
		}
	}


	public EventNotificationModel()
	{
		setExpandAllCommand(new UICommand("ExpandAll", this));
		setCollapseAllCommand(new UICommand("CollapseAll", this));

		setEmail(new EntityModel());
	}

	public void ExpandAll()
	{
		//EventGroupModels.Each(a => a.IsExpanded = true);
		for (SelectionTreeNodeModel a : getEventGroupModels())
		{
			a.setIsExpanded(true);
		}
	}

	public void CollapseAll()
	{
		//EventGroupModels.Each(a => a.IsExpanded = false);
		for (SelectionTreeNodeModel a : getEventGroupModels())
		{
			a.setIsExpanded(false);
		}
	}

	public boolean Validate()
	{
		getEmail().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new EmailValidation() });

		return getEmail().getIsValid();
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getExpandAllCommand())
		{
			ExpandAll();
		}
		if (command == getCollapseAllCommand())
		{
			CollapseAll();
		}
	}

}