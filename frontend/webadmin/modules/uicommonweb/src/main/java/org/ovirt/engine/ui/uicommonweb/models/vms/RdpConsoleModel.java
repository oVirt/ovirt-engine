package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RdpConsoleModel extends ConsoleModel
{
    public static EventDefinition RdpDisconnectedEventDefinition = new EventDefinition("RdpDisconnected", //$NON-NLS-1$
            RdpConsoleModel.class);

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
        setTitle(ConstantsManager.getInstance().getConstants().RDPTitle());

        setrdp((IRdp) TypeResolver.getInstance().Resolve(IRdp.class));
    }

    @Override
    protected void Connect()
    {
        if (getEntity() != null)
        {
            getLogger().Debug("Connecting to RDP console..."); //$NON-NLS-1$

            getrdp().setAddress(getEntity().getvm_host().split("[ ]", -1)[0]); //$NON-NLS-1$
            getrdp().setGuestID(getEntity().getId().toString());

            // Subscribe to disconnect event
            if (getrdp().getDisconnectedEvent() != null)
            {
                getrdp().getDisconnectedEvent().addListener(this);
            }

            // Try to connect.
            try
            {
                getrdp().Connect();
                UpdateActionAvailability();
            } catch (RuntimeException ex)
            {
                getLogger().Error("Exception on RDP connect", ex); //$NON-NLS-1$
            }
        }
    }

    @Override
    protected void UpdateActionAvailability()
    {
        super.UpdateActionAvailability();

        getConnectCommand().setIsExecutionAllowed(getEntity() != null
                && (getEntity().getstatus() == VMStatus.Up || getEntity().getstatus() == VMStatus.PoweringDown)
                && DataProvider.IsWindowsOsType(getEntity().getvm_os()) && getConfigurator().isClientWindownsExplorer());
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (getrdp().getDisconnectedEvent() != null && ev.equals(getrdp().getDisconnectedEvent()))
        {
            Rdp_Disconnected(sender, (ErrorCodeEventArgs) args);
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
