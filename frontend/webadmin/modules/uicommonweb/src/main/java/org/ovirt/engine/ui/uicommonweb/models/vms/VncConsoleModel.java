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
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class VncConsoleModel extends ConsoleModel
{
    private IVnc vnc;

    public VncConsoleModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().VNCTitle());

        vnc = (IVnc) TypeResolver.getInstance().Resolve(IVnc.class);
    }

    @Override
    protected void Connect()
    {
        if (getEntity() != null)
        {
            getLogger().Debug("Connecting to VNC console..."); //$NON-NLS-1$

            // Don't connect if there VM is not running on any host.
            if (getEntity().getrun_on_vds() == null)
            {
                return;
            }

            // Determine the display IP.
            String displayIp = getEntity().getdisplay_ip();
            if (StringHelper.isNullOrEmpty(getEntity().getdisplay_ip())
                    || StringHelper.stringsEqual(getEntity().getdisplay_ip(), "0")) //$NON-NLS-1$
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
                    Frontend.RunAction(VdcActionType.SetVmTicket, new SetVmTicketParameters(getEntity().getId(),
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
                getLogger().Error("Exception on VNC connect", ex); //$NON-NLS-1$
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
