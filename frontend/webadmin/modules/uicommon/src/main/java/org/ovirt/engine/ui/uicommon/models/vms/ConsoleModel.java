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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public abstract class ConsoleModel extends EntityModel
{
	public static final String EjectLabel = "[Eject]";


	public static EventDefinition ErrorEventDefinition;
	private Event privateErrorEvent;
	public Event getErrorEvent()
	{
		return privateErrorEvent;
	}
	private void setErrorEvent(Event value)
	{
		privateErrorEvent = value;
	}



	private UICommand privateConnectCommand;
	public UICommand getConnectCommand()
	{
		return privateConnectCommand;
	}
	private void setConnectCommand(UICommand value)
	{
		privateConnectCommand = value;
	}



	private boolean isConnected;
	public boolean getIsConnected()
	{
		return isConnected;
	}
	public void setIsConnected(boolean value)
	{
		if (isConnected != value)
		{
			isConnected = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsConnected"));
		}
	}

	private boolean forceVmStatusUp;
	public boolean getForceVmStatusUp()
	{
		return forceVmStatusUp;
	}
	public void setForceVmStatusUp(boolean value)
	{
		if (forceVmStatusUp != value)
		{
			forceVmStatusUp = value;
			OnPropertyChanged(new PropertyChangedEventArgs("ForceVmStatusUp"));
		}
	}

	public VM getEntity()
	{
		return (VM)super.getEntity();
	}
	public void setEntity(VM value)
	{
		super.setEntity(value);
	}


	static
	{
		ErrorEventDefinition = new EventDefinition("Error", ConsoleModel.class);
	}

	protected ConsoleModel()
	{
		setErrorEvent(new Event(ErrorEventDefinition));

		setConnectCommand(new UICommand("Connect", this));
	}

	protected abstract void Connect();

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		UpdateActionAvailability();
	}

	@Override
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);

		if (e.PropertyName.equals("status"))
		{
			UpdateActionAvailability();
		}
	}

	protected void UpdateActionAvailability()
	{
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getConnectCommand())
		{
			Connect();
		}
	}

	public boolean IsVmConnectReady()
	{
		if (getForceVmStatusUp())
		{
			return getEntity().getstatus() == VMStatus.Up;
		}

		return IsVmUp();
	}

	public boolean IsVmUp()
	{
		switch (getEntity().getstatus())
		{
			case PoweringUp:
			case Up:
			case RebootInProgress:
			case PoweringDown:
			case Paused:
				return true;

			default:
				return false;
		}
	}
}