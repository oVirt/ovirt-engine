package org.ovirt.engine.ui.uicommonweb;
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

import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.ui.uicompat.*;

/**
 Represents a command adapted to use in model-viewmodel pattern + binding.
*/
@SuppressWarnings("unused")
public class UICommand extends Model implements ICommand
{

	private boolean isExecutionAllowed;
	/**
	 Gets or sets the flag indincating whether this command
	 is available but can't be executed from some reasons.
	*/
	public boolean getIsExecutionAllowed()
	{
		return isExecutionAllowed;
	}
	public void setIsExecutionAllowed(boolean value)
	{
		if (isExecutionAllowed != value)
		{
			isExecutionAllowed = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsExecutionAllowed"));

			if (getIsExecutionAllowed())
			{
				getExecuteProhibitionReasons().clear();
			}
		}
	}

	private java.util.List<String> privateExecuteProhibitionReasons;
	public java.util.List<String> getExecuteProhibitionReasons()
	{
		return privateExecuteProhibitionReasons;
	}
	public void setExecuteProhibitionReasons(java.util.List<String> value)
	{
		privateExecuteProhibitionReasons = value;
	}

	private boolean privateIsDefault;
	public boolean getIsDefault()
	{
		return privateIsDefault;
	}
	public void setIsDefault(boolean value)
	{
		privateIsDefault = value;
	}
	private boolean privateIsCancel;
	public boolean getIsCancel()
	{
		return privateIsCancel;
	}
	public void setIsCancel(boolean value)
	{
		privateIsCancel = value;
	}

	private String privateName;
	public String getName()
	{
		return privateName;
	}
	public void setName(String value)
	{
		privateName = value;
	}


	private ICommandTarget target;

	public UICommand(String name, ICommandTarget target)
	{
		this();
		setName(name);
		this.target = target;
	}

	private UICommand()
	{
		setExecuteProhibitionReasons(new ObservableCollection<String>());
		setIsExecutionAllowed(true);
	}


	public boolean CanExecute(Object parameter)
	{
		return true;
	}

//C# TO JAVA CONVERTER TODO TASK: Events are not available in Java:
//	public event EventHandler CanExecuteChanged;

	public void Execute(Object parameter)
	{
		if (!getIsAvailable() || !getIsExecutionAllowed())
		{
			return;
		}

		if (target != null)
		{
			target.ExecuteCommand(this);
		}
	}


	public void Execute()
	{
		Execute(null);
	}
}