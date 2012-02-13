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
public class RdpConsoleModel extends ConsoleModel
{
	public static EventDefinition RdpDisconnectedEventDefinition = new EventDefinition("RdpDisconnected", RdpConsoleModel.class);

	private IRdp privaterdp;
	public IRdp getrdp()
	{
		return privaterdp;
	}
	private void setrdp(IRdp value)
	{
		privaterdp = value;
	}

	public RdpConsoleModel()
	{
		setTitle("RDP");

		setrdp((IRdp)TypeResolver.getInstance().Resolve(IRdp.class));
	}

	@Override
	protected void Connect()
	{
		if (getEntity() != null)
		{
			getLogger().Debug("Connecting to RDP console...");

			getrdp().setAddress(getEntity().getvm_host().split("[ ]", -1)[0]);
			getrdp().setGuestID(getEntity().getId().toString());

			//Subscribe to disconnect event
			if (getrdp().getDisconnectedEvent() != null)
			{
				getrdp().getDisconnectedEvent().addListener(this);
			}

			//Try to connect.
			try
			{
				getrdp().Connect();
				UpdateActionAvailability();
			}
			catch (RuntimeException ex)
			{
				getLogger().Error("Exception on RDP connect", ex);
			}
		}
	}

	@Override
	protected void UpdateActionAvailability()
	{
		super.UpdateActionAvailability();

		getConnectCommand().setIsExecutionAllowed(getEntity() != null && (getEntity().getstatus() == VMStatus.Up || getEntity().getstatus() == VMStatus.PoweringDown) && DataProvider.IsWindowsOsType(getEntity().getvm_os()));
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (getrdp().getDisconnectedEvent() != null && ev.equals(getrdp().getDisconnectedEvent()))
		{
			Rdp_Disconnected(sender, (ErrorCodeEventArgs)args);
		}
	}

	private void Rdp_Disconnected(Object sender, ErrorCodeEventArgs e)
	{
		getrdp().getDisconnectedEvent().removeListener(this);

		if (e.getErrorCode() > 100)
		{
			getErrorEvent().raise(this, e);
		}
	}
}