package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;

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

        setrdp((IRdp) TypeResolver.getInstance().resolve(IRdp.class));
    }

    @Override
    protected void connect()
    {
        if (getEntity() != null)
        {
            getLogger().debug("Connecting to RDP console..."); //$NON-NLS-1$

            getrdp().setAddress(getEntity().getVmHost().split("[ ]", -1)[0]); //$NON-NLS-1$
            getrdp().setGuestID(getEntity().getId().toString());

            // Subscribe to disconnect event
            if (getrdp().getDisconnectedEvent() != null)
            {
                getrdp().getDisconnectedEvent().addListener(this);
            }

            // Try to connect.
            try
            {
                getrdp().connect();
                updateActionAvailability();
            } catch (RuntimeException ex)
            {
                getLogger().error("Exception on RDP connect", ex); //$NON-NLS-1$
            }
        }
    }

    @Override
    protected void updateActionAvailability()
    {
        super.updateActionAvailability();

        getConnectCommand().setIsExecutionAllowed(getEntity() != null
                && (getEntity().getStatus() == VMStatus.Up || getEntity().getStatus() == VMStatus.PoweringDown)
                && AsyncDataProvider.isWindowsOsType(getEntity().getVmOs()));
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (getrdp().getDisconnectedEvent() != null && ev.equals(getrdp().getDisconnectedEvent()))
        {
            rdp_Disconnected(sender, (ErrorCodeEventArgs) args);
        }
    }

    private void rdp_Disconnected(Object sender, ErrorCodeEventArgs e)
    {
        getrdp().getDisconnectedEvent().removeListener(this);

        if (e.getErrorCode() > 100)
        {
            getErrorEvent().raise(this, e);
        }
    }
}
