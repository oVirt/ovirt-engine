package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class VncConsoleModel extends ConsoleModel
{
    String otp64 = null;
    private static final int seconds = 120;

    public VncConsoleModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().VNCTitle());
    }

    @Override
    protected void Connect()
    {
        if (getEntity() == null || getEntity().getRunOnVds() == null) {
            return;
        }
        getLogger().Debug("VNC console info..."); //$NON-NLS-1$

        UICommand setVmTicketCommand = new UICommand("setVmCommand", new BaseCommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand uiCommand) {
                setVmTicket();
            }
        });

        executeCommandWithConsoleSafenessWarning(setVmTicketCommand);
    }

    private void setVmTicket() {
        Frontend.RunAction(VdcActionType.SetVmTicket, new SetVmTicketParameters(getEntity().getId(),
                    null,
                    seconds), new IFrontendActionAsyncCallback() {

                @Override
                public void Executed(FrontendActionAsyncResult result) {

                    VdcReturnValueBase ticketReturnValue = result.getReturnValue();
                    if (ticketReturnValue != null && ticketReturnValue.getActionReturnValue() != null)
                    {
                        otp64 = (String) ticketReturnValue.getActionReturnValue();
                        // Determine the display IP.
                        if (StringHelper.isNullOrEmpty(getEntity().getDisplayIp())
                                || StringHelper.stringsEqual(getEntity().getDisplayIp(), "0")) //$NON-NLS-1$
                        {
                            AsyncQuery _asyncQuery = new AsyncQuery();
                            _asyncQuery.setModel(this);
                            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                                @Override
                                public void onSuccess(Object model, Object ReturnValue)
                                {
                                    VncConsoleModel consoleModel = (VncConsoleModel) model;
                                    consoleModel.postGetHost((String) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                                }
                            };

                            Frontend.RunQuery(VdcQueryType.GetManagementInterfaceAddressByVmId,
                                    new IdQueryParameters(getEntity().getId()), _asyncQuery);
                        }
                        else {
                            postGetHost(getEntity().getDisplayIp());
                        }
                    }
                }
            });
    }

    protected void postGetHost(String hostName) {
        StringBuilder configBuilder = new StringBuilder("[virt-viewer]"); //$NON-NLS-1$
        configBuilder.append("\ntype=vnc") //$NON-NLS-1$
            .append("\nhost=").append(hostName) //$NON-NLS-1$
            .append("\nport=").append(getEntity().getDisplay().toString()) //$NON-NLS-1$
            .append("\npassword=").append(otp64) //$NON-NLS-1$
            .append("\ntitle=").append(getTitle()); //$NON-NLS-1$

        ConsoleModel.makeConsoleConfigRequest("console.vv", "application/x-virt-viewer; charset=UTF-8", configBuilder.toString()); //$NON-NLS-1$ $NON-NLS-2$
    }

    @Override
    protected void UpdateActionAvailability()
    {
        super.UpdateActionAvailability();

        getConnectCommand().setIsExecutionAllowed(getEntity() != null
                && getEntity().getDisplayType() == DisplayType.vnc
                && (getEntity().getStatus() == VMStatus.PoweringUp || getEntity().getStatus() == VMStatus.Up
                        || getEntity().getStatus() == VMStatus.RebootInProgress
                        || getEntity().getStatus() == VMStatus.PoweringDown || getEntity().getStatus() == VMStatus.Paused));
    }
}
