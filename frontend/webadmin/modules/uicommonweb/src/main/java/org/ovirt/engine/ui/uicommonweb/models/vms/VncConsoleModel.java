package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;

@SuppressWarnings("unused")
public class VncConsoleModel extends ConsoleModel
{
    private IVnc vnc;

    public VncConsoleModel()
    {
        setTitle("VNC");

        vnc = (IVnc) TypeResolver.getInstance().Resolve(IVnc.class);
    }

    @Override
    protected void Connect()
    {
        if (getEntity() != null)
        {
            getLogger().Debug("Connecting to VNC console...");

            // Don't connect if there VM is not running on any host.
            if (getEntity().getrun_on_vds() == null)
            {
                return;
            }

            // Determine the display IP.
            String displayIp = getEntity().getdisplay_ip();
            if (StringHelper.isNullOrEmpty(getEntity().getdisplay_ip())
                    || StringHelper.stringsEqual(getEntity().getdisplay_ip(), "0"))
            {
                VDS host = DataProvider.GetHostById(getEntity().getrun_on_vds().getValue());
                if (host == null)
                {
                    return;
                }

                displayIp = host.gethost_name();
            }

            String otp64 = null;
            VdcReturnValueBase ticketReturnValue =
                    Frontend.RunAction(VdcActionType.SetVmTicket, new SetVmTicketParameters(getEntity().getvm_guid(),
                            null,
                            120));

            if (ticketReturnValue != null && ticketReturnValue.getActionReturnValue() != null)
            {
                otp64 = (String) ticketReturnValue.getActionReturnValue();
            }

            vnc.setHost(displayIp);
            vnc.setPort((getEntity().getdisplay() == null ? 0 : getEntity().getdisplay()));
            vnc.setPassword(otp64);
            vnc.setTitle(getEntity().getvm_name());

            // Try to connect.
            try
            {
                vnc.Connect();
                UpdateActionAvailability();
            } catch (RuntimeException ex)
            {
                getLogger().Error("Exception on VNC connect", ex);
            }
        }
    }

    @Override
    protected void UpdateActionAvailability()
    {
        super.UpdateActionAvailability();

        getConnectCommand().setIsExecutionAllowed(getConfigurator().IsDisplayTypeSupported(DisplayType.vnc)
                && getEntity() != null
                && getEntity().getdisplay_type() == DisplayType.vnc
                && (getEntity().getstatus() == VMStatus.PoweringUp || getEntity().getstatus() == VMStatus.Up
                        || getEntity().getstatus() == VMStatus.RebootInProgress
                        || getEntity().getstatus() == VMStatus.PoweringDown || getEntity().getstatus() == VMStatus.Paused));
    }
}
