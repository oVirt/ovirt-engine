package org.ovirt.engine.ui.uicommon.models.configure;
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
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class ChangeCDModel extends Model
{

//		public event EventHandler Executed = delegate { };

	public static EventDefinition ExecutedEventDefinition;
	private Event privateExecutedEvent;
	public Event getExecutedEvent()
	{
		return privateExecutedEvent;
	}
	private void setExecutedEvent(Event value)
	{
		privateExecutedEvent = value;
	}



	private UICommand privateDoCommand;
	public UICommand getDoCommand()
	{
		return privateDoCommand;
	}
	private void setDoCommand(UICommand value)
	{
		privateDoCommand = value;
	}




	static
	{
		ExecutedEventDefinition = new EventDefinition("Executed", ChangeCDModel.class);
	}

	public ChangeCDModel()
	{
		setExecutedEvent(new Event(ExecutedEventDefinition));

		setDoCommand(new UICommand("Do", this));
	}

	private void Do()
	{
		getExecutedEvent().raise(this, EventArgs.Empty);
//			Executed(this, EventArgs.Empty);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getDoCommand())
		{
			Do();
		}
	}
}